import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ProjectService, Project } from './project.service';
import { SharedModule } from '../../shared/shared.module';

@Component({
  selector: 'app-project-form',
  standalone: true,
  imports: [SharedModule],
  template: `
    <h2 mat-dialog-title>{{ data.id ? 'Edit' : 'New' }} Project</h2>
    <mat-dialog-content>
      <form [formGroup]="form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Name</mat-label>
          <input matInput formControlName="name">
        </mat-form-field>
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Description</mat-label>
          <textarea matInput formControlName="description" rows="3"></textarea>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-raised-button color="primary" [disabled]="form.invalid" (click)="save()">Save</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .full-width { width: 100%; margin-bottom: 10px; }
  `]
})
export class ProjectFormComponent {
  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private projectService: ProjectService,
    public dialogRef: MatDialogRef<ProjectFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Project
  ) {
    this.form = this.fb.group({
      name: [data.name || '', Validators.required],
      description: [data.description || '']
    });
  }

  save() {
    if (this.form.valid) {
      const project = { ...this.data, ...this.form.value };
      if (project.id) {
        this.projectService.updateProject(project.id, project).subscribe(() => this.dialogRef.close(true));
      } else {
        this.projectService.createProject(project).subscribe(() => this.dialogRef.close(true));
      }
    }
  }
}
