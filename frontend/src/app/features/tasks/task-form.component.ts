import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TaskService } from './task.service';
import { EpicService, Epic } from '../epics/epic.service';
import { MilestoneService, Milestone } from '../milestones/milestone.service';
import { SharedModule } from '../../shared/shared.module';
import { PermissionService } from '../../core/permission.service';

@Component({
  selector: 'app-task-form',
  standalone: true,
  imports: [SharedModule],
  template: `
    <h2 mat-dialog-title>{{ data.task.id ? 'Edit' : 'New' }} Task</h2>
    <mat-dialog-content>
      <form [formGroup]="form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Title</mat-label>
          <input matInput formControlName="title" required>
          <mat-error>Title is required</mat-error>
        </mat-form-field>
        
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Description</mat-label>
          <textarea matInput formControlName="description" rows="3"></textarea>
        </mat-form-field>

        <div class="row">
          <mat-form-field appearance="outline">
            <mat-label>Status</mat-label>
            <mat-select formControlName="status" required>
              <mat-option value="TO_DO">To Do</mat-option>
              <mat-option value="IN_PROGRESS">In Progress</mat-option>
              <mat-option value="DONE">Done</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Priority</mat-label>
            <mat-select formControlName="priority" required>
              <mat-option value="LOW">Low</mat-option>
              <mat-option value="MEDIUM">Medium</mat-option>
              <mat-option value="HIGH">High</mat-option>
            </mat-select>
          </mat-form-field>
        </div>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Due Date</mat-label>
          <input matInput [matDatepicker]="picker" formControlName="dueDate">
          <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
          <mat-datepicker #picker></mat-datepicker>
        </mat-form-field>

        <!-- Epic Linking -->
        <mat-form-field appearance="outline" class="full-width" *ngIf="epics.length > 0">
          <mat-label>Link to Epic (Optional)</mat-label>
          <mat-select formControlName="epicId">
            <mat-option value="">None</mat-option>
            <mat-option *ngFor="let epic of epics" [value]="epic.id">
              {{ epic.title }}
            </mat-option>
          </mat-select>
          <mat-hint>Link this task to an existing epic</mat-hint>
        </mat-form-field>

        <!-- Milestone Linking -->
        <mat-form-field appearance="outline" class="full-width" *ngIf="milestones.length > 0">
          <mat-label>Link to Milestone (Optional)</mat-label>
          <mat-select formControlName="milestoneId">
            <mat-option value="">None</mat-option>
            <mat-option *ngFor="let milestone of milestones" [value]="milestone.id">
              {{ milestone.title }}
            </mat-option>
          </mat-select>
          <mat-hint>Link this task to an existing milestone</mat-hint>
        </mat-form-field>

        <!-- Approval Request Section -->
        <mat-divider class="divider-margin"></mat-divider>
        <div class="approval-section" *ngIf="canRequestApproval()">
          <h3>Approval Workflow</h3>
          <mat-checkbox formControlName="requestApproval">
            Request Approval for this Task
          </mat-checkbox>
          
          <mat-form-field appearance="outline" class="full-width" *ngIf="form.get('requestApproval')?.value">
            <mat-label>Approval Comments (Optional)</mat-label>
            <textarea matInput formControlName="approvalComments" rows="2" 
                      placeholder="Add comments for the approver"></textarea>
          </mat-form-field>
        </div>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-raised-button color="primary" [disabled]="form.invalid || loading" (click)="save()">
        {{ loading ? 'Saving...' : 'Save' }}
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .full-width { width: 100%; margin-bottom: 15px; }
    .row { display: flex; gap: 15px; }
    .row mat-form-field { flex: 1; }
    .divider-margin { margin: 20px 0; }
    .approval-section { margin-top: 20px; padding-top: 20px; }
    .approval-section h3 { margin-top: 0; color: #666; font-size: 14px; text-transform: uppercase; }
    .approval-section mat-checkbox { display: block; margin-bottom: 15px; }
  `]
})
export class TaskFormComponent implements OnInit {
  form: FormGroup;
  loading = false;
  epics: Epic[] = [];
  milestones: Milestone[] = [];

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService,
    private epicService: EpicService,
    private milestoneService: MilestoneService,
    private permissionService: PermissionService,
    public dialogRef: MatDialogRef<TaskFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.form = this.fb.group({
      title: [data.task?.title || '', Validators.required],
      description: [data.task?.description || ''],
      status: [data.task?.status || 'TO_DO', Validators.required],
      priority: [data.task?.priority || 'MEDIUM', Validators.required],
      dueDate: [data.task?.dueDate || null],
      epicId: [data.task?.epicId || null],
      milestoneId: [data.task?.milestoneId || null],
      projectId: [data.projectId || data.task?.projectId || '', Validators.required],
      requestApproval: [false],
      approvalComments: ['']
    });
  }

  ngOnInit() {
    this.loadEpicsAndMilestones();
  }

  loadEpicsAndMilestones() {
    if (this.data.projectId) {
      // Load epics
      this.epicService.getEpicsByProject(this.data.projectId).subscribe({
        next: (epics) => this.epics = epics,
        error: (err) => console.error('Error loading epics:', err)
      });

      // Load milestones
      this.milestoneService.getMilestonesByProject(this.data.projectId).subscribe({
        next: (milestones) => this.milestones = milestones,
        error: (err) => console.error('Error loading milestones:', err)
      });
    }
  }

  canRequestApproval(): boolean {
    return this.permissionService.canPerformAction('REQUEST_APPROVAL');
  }

  save() {
    if (this.form.valid) {
      this.loading = true;
      const task = { ...this.data.task, ...this.form.value };
      
      if (task.id) {
        this.taskService.updateTask(task.id, task).subscribe({
          next: () => this.dialogRef.close(true),
          error: (err) => {
            console.error('Error updating task:', err);
            this.loading = false;
          }
        });
      } else {
        this.taskService.createTask(task).subscribe({
          next: () => this.dialogRef.close(true),
          error: (err) => {
            console.error('Error creating task:', err);
            this.loading = false;
          }
        });
      }
    }
  }
}
