import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { EpicService } from './epic.service';
import { SharedModule } from '../../shared/shared.module';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PermissionService } from '../../core/permission.service';

@Component({
  selector: 'app-epic-form',
  standalone: true,
  imports: [SharedModule],
  template: `
    <h2 mat-dialog-title>{{ data.isEdit ? 'Edit' : 'Create' }} Epic</h2>
    <mat-dialog-content>
      <form [formGroup]="form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Title</mat-label>
          <input matInput formControlName="title" placeholder="Enter epic title">
          <mat-error *ngIf="form.get('title')?.hasError('required')">Title is required</mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Description</mat-label>
          <textarea matInput 
                    formControlName="description" 
                    rows="3" 
                    placeholder="Enter epic description"></textarea>
        </mat-form-field>

        <div class="row">
          <mat-form-field appearance="outline">
            <mat-label>Status</mat-label>
            <mat-select formControlName="status">
              <mat-option value="OPEN">Open</mat-option>
              <mat-option value="IN_PROGRESS">In Progress</mat-option>
              <mat-option value="CLOSED">Closed</mat-option>
            </mat-select>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Visibility</mat-label>
            <mat-select formControlName="visibilityLevel">
              <mat-option value="TEAM">Team</mat-option>
              <mat-option value="PUBLIC">Public</mat-option>
              <mat-option value="PRIVATE">Private</mat-option>
            </mat-select>
          </mat-form-field>
        </div>

        <!-- Approval Request Section -->
        <mat-divider class="divider-margin"></mat-divider>
        <div class="approval-section" *ngIf="canRequestApproval()">
          <h3>Approval Workflow</h3>
          <mat-checkbox formControlName="requestApproval">
            Request Approval for this Epic
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

    .row mat-form-field {
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
export class EpicFormComponent {
  form: FormGroup;
  loading = false;

  constructor(
    private fb: FormBuilder,
    private epicService: EpicService,
    private permissionService: PermissionService,
    public dialogRef: MatDialogRef<EpicFormComponent>,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.form = this.fb.group({
      title: [data?.epic?.title || '', Validators.required],
      description: [data?.epic?.description || ''],
      status: [data?.epic?.status || 'OPEN', Validators.required],
      visibilityLevel: [data?.epic?.visibilityLevel || 'TEAM', Validators.required],
      requestApproval: [false],
      approvalComments: ['']
    });
  }

  canRequestApproval(): boolean {
    return this.permissionService.canPerformAction('REQUEST_APPROVAL');
  }

  save() {
    if (this.form.valid && this.data.projectId) {
      this.loading = true;
      const formValue = this.form.value;

      if (this.data.isEdit) {
        this.epicService.updateEpic(
          this.data.projectId,
          this.data.epic.id,
          formValue.title,
          formValue.description,
          formValue.status
        ).subscribe({
          next: () => {
            this.snackBar.open('Epic updated successfully', 'Close', { duration: 3000 });
            this.dialogRef.close(true);
          },
          error: (error) => {
            this.loading = false;
            this.snackBar.open('Failed to update epic', 'Close', { duration: 3000 });
            console.error(error);
          }
        });
      } else {
        this.epicService.createEpic(
          this.data.projectId,
          formValue.title,
          formValue.description,
          formValue.visibilityLevel
        ).subscribe({
          next: (epic: any) => {
            // Request approval if flag is set
            if (formValue.requestApproval) {
              this.epicService.requestApproval(epic.id, formValue.approvalComments).subscribe({
                next: () => {
                  this.snackBar.open('Epic created and approval request submitted', 'Close', { duration: 3000 });
                  this.dialogRef.close(true);
                },
                error: (error) => {
                  this.loading = false;
                  console.error('Error requesting approval:', error);
                  this.snackBar.open('Epic created but approval request failed', 'Close', { duration: 3000 });
                }
              });
            } else {
              this.snackBar.open('Epic created successfully', 'Close', { duration: 3000 });
              this.dialogRef.close(true);
            }
          },
          error: (error) => {
            this.loading = false;
            this.snackBar.open('Failed to create epic', 'Close', { duration: 3000 });
            console.error(error);
          }
        });
      }
    }
  }
}
