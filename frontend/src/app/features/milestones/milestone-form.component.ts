import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MilestoneService } from './milestone.service';
import { EpicService } from '../epics/epic.service';
import { SharedModule } from '../../shared/shared.module';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Epic } from '../epics/epic.service';
import { PermissionService } from '../../core/permission.service';

@Component({
  selector: 'app-milestone-form',
  standalone: true,
  imports: [SharedModule],
  template: `
    <h2 mat-dialog-title>{{ data.isEdit ? 'Edit' : 'Create' }} Milestone</h2>
    <mat-dialog-content>
      <form [formGroup]="form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Title</mat-label>
          <input matInput formControlName="title" placeholder="Enter milestone title">
          <mat-error *ngIf="form.get('title')?.hasError('required')">Title is required</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Description</mat-label>
          <textarea matInput 
                    formControlName="description" 
                    rows="3" 
                    placeholder="Enter milestone description"></textarea>
        </mat-form-field>

        <div class="row">
          <mat-form-field appearance="outline" class="flex-grow">
            <mat-label>Target Date</mat-label>
            <input matInput [matDatepicker]="picker" formControlName="targetDate">
            <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
            <mat-datepicker #picker></mat-datepicker>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Status</mat-label>
            <mat-select formControlName="status">
              <mat-option value="OPEN">Open</mat-option>
              <mat-option value="IN_PROGRESS">In Progress</mat-option>
              <mat-option value="CLOSED">Closed</mat-option>
            </mat-select>
          </mat-form-field>
        </div>

        <mat-form-field appearance="outline" class="full-width" *ngIf="epics.length > 0">
          <mat-label>Epic (Optional)</mat-label>
          <mat-select formControlName="epicId">
            <mat-option value="">None</mat-option>
            <mat-option *ngFor="let epic of epics" [value]="epic.id">
              {{ epic.title }}
            </mat-option>
          </mat-select>
        </mat-form-field>

        <!-- Approval Request Section -->
        <mat-divider class="divider-margin"></mat-divider>
        <div class="approval-section" *ngIf="canRequestApproval()">
          <h3>Approval Workflow</h3>
          <mat-checkbox formControlName="requestApproval">
            Request Approval for this Milestone
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
    .full-width {
      width: 100%;
      margin-bottom: 15px;
    }

    .row {
      display: flex;
      gap: 15px;
    }

    .flex-grow {
      flex: 1;
    }

    .row mat-form-field:not(.flex-grow) {
      flex: 1;
    }

    .divider-margin {
      margin: 20px 0;
    }

    .approval-section {
      margin-top: 20px;
      padding-top: 20px;
    }

    .approval-section h3 {
      margin-top: 0;
      color: #666;
      font-size: 14px;
      text-transform: uppercase;
    }

    .approval-section mat-checkbox {
      display: block;
      margin-bottom: 15px;
    }
  `]
})
export class MilestoneFormComponent implements OnInit {
  form: FormGroup;
  loading = false;
  epics: Epic[] = [];

  constructor(
    private fb: FormBuilder,
    private milestoneService: MilestoneService,
    private epicService: EpicService,
    private permissionService: PermissionService,
    public dialogRef: MatDialogRef<MilestoneFormComponent>,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.form = this.fb.group({
      title: [data?.milestone?.title || '', Validators.required],
      description: [data?.milestone?.description || ''],
      targetDate: [data?.milestone?.targetDate || ''],
      status: [data?.milestone?.status || 'OPEN', Validators.required],
      epicId: [data?.milestone?.epicId || ''],
      requestApproval: [false],
      approvalComments: ['']
    });
  }

  ngOnInit() {
    this.loadEpics();
  }

  loadEpics() {
    if (this.data.projectId) {
      this.epicService.getEpics(this.data.projectId).subscribe({
        next: (data) => {
          this.epics = data;
        },
        error: (error) => {
          console.error('Failed to load epics:', error);
        }
      });
    }
  }

  canRequestApproval(): boolean {
    return this.permissionService.canPerformAction('REQUEST_APPROVAL');
  }

  save() {
    if (this.form.valid && this.data.projectId) {
      this.loading = true;
      const formValue = this.form.value;

      if (this.data.isEdit) {
        this.milestoneService.updateMilestone(
          this.data.projectId,
          this.data.milestone.id,
          formValue.title,
          formValue.description,
          formValue.targetDate,
          formValue.status
        ).subscribe({
          next: () => {
            this.snackBar.open('Milestone updated successfully', 'Close', { duration: 3000 });
            this.dialogRef.close(true);
          },
          error: (error) => {
            this.loading = false;
            this.snackBar.open('Failed to update milestone', 'Close', { duration: 3000 });
            console.error(error);
          }
        });
      } else {
        this.milestoneService.createMilestone(
          this.data.projectId,
          formValue.title,
          formValue.epicId || undefined,
          formValue.description,
          formValue.targetDate || undefined
        ).subscribe({
          next: (milestone: any) => {
            // Request approval if flag is set
            if (formValue.requestApproval) {
              this.milestoneService.requestApproval(milestone.id, formValue.approvalComments).subscribe({
                next: () => {
                  this.snackBar.open('Milestone created and approval request submitted', 'Close', { duration: 3000 });
                  this.dialogRef.close(true);
                },
                error: (error) => {
                  this.loading = false;
                  console.error('Error requesting approval:', error);
                  this.snackBar.open('Milestone created but approval request failed', 'Close', { duration: 3000 });
                }
              });
            } else {
              this.snackBar.open('Milestone created successfully', 'Close', { duration: 3000 });
              this.dialogRef.close(true);
            }
          },
          error: (error) => {
            this.loading = false;
            this.snackBar.open('Failed to create milestone', 'Close', { duration: 3000 });
            console.error(error);
          }
        });
      }
    }
  }
}
