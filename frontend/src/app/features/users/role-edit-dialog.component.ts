import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../../shared/shared.module';
import { User } from './user.service';

@Component({
  selector: 'app-role-edit-dialog',
  standalone: true,
  imports: [CommonModule, SharedModule, ReactiveFormsModule],
  template: `
    <h2 mat-dialog-title>Edit Roles for {{ data.user.username }}</h2>
    <mat-dialog-content>
      <form [formGroup]="roleForm">
        <mat-form-field appearance="fill" class="w-full">
          <mat-label>Roles</mat-label>
          <mat-select formControlName="roles" multiple>
            <mat-option *ngFor="let role of availableRoles" [value]="role">
              {{ role }}
            </mat-option>
          </mat-select>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-raised-button color="primary" (click)="onSave()" [disabled]="roleForm.invalid">Save</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .w-full { width: 100%; }
  `]
})
export class RoleEditDialogComponent {
  roleForm: FormGroup;
  availableRoles = ['ADMIN', 'MANAGER', 'TECHNICAL_LEAD', 'USER'];

  constructor(
    public dialogRef: MatDialogRef<RoleEditDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { user: User },
    private fb: FormBuilder
  ) {
    this.roleForm = this.fb.group({
      roles: [data.user.roles || []]
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.roleForm.valid) {
      this.dialogRef.close(this.roleForm.value.roles);
    }
  }
}
