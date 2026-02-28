import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ProjectService, Project } from './project.service';
import { TemplateService, ProjectTemplate } from '../templates/template.service';
import { SharedModule } from '../../shared/shared.module';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-project-form-with-template',
  standalone: true,
  imports: [SharedModule, CommonModule],
  template: `
    <h2 mat-dialog-title>{{ data.id ? 'Edit' : 'New' }} Project</h2>
    <mat-dialog-content>
      <form [formGroup]="form">
        <div *ngIf="!data.id" class="template-section">
          <h3>Create from Template (Optional)</h3>
          <mat-form-field appearance="outline" class="full-width">
            <mat-label>Select Template</mat-label>
            <mat-select formControlName="templateId" (selectionChange)="onTemplateChange($event.value)">
              <mat-option [value]="null">Start from scratch</mat-option>
              <mat-option *ngFor="let template of templates" [value]="template.id">
                {{ template.name }} ({{ template.templateType }})
              </mat-option>
            </mat-select>
          </mat-form-field>
          <div *ngIf="selectedTemplate" class="template-info">
            <p><strong>Template:</strong> {{ selectedTemplate.name }}</p>
            <p *ngIf="selectedTemplate.templateTasks">
              <strong>Tasks:</strong> {{ selectedTemplate.templateTasks.length }} tasks will be created
            </p>
          </div>
        </div>

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
    .template-section { margin-bottom: 20px; padding: 15px; background: #f5f5f5; border-radius: 8px; }
    .template-info { margin-top: 10px; padding: 10px; background: white; border-radius: 4px; }
  `]
})
export class ProjectFormWithTemplateComponent {
  form: FormGroup;
  templates: ProjectTemplate[] = [];
  selectedTemplate: ProjectTemplate | null = null;

  constructor(
    private fb: FormBuilder,
    private projectService: ProjectService,
    private templateService: TemplateService,
    public dialogRef: MatDialogRef<ProjectFormWithTemplateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Project
  ) {
    this.form = this.fb.group({
      name: [data?.name || '', Validators.required],
      description: [data?.description || ''],
      templateId: [null]
    });

    if (!data?.id) {
      this.loadTemplates();
    }
  }

  loadTemplates() {
    this.templateService.getPublicTemplates().subscribe(data => {
      this.templates = data;
    });
  }

  onTemplateChange(templateId: string | null) {
    if (templateId) {
      this.templateService.getTemplateById(templateId).subscribe(template => {
        this.selectedTemplate = template;
      });
    } else {
      this.selectedTemplate = null;
    }
  }

  save() {
    if (this.form.valid) {
      const project = { ...this.data, ...this.form.value };
      const templateId = this.form.value.templateId;

      if (project.id) {
        this.projectService.updateProject(project.id, project).subscribe(() => this.dialogRef.close(true));
      } else {
        if (templateId) {
          // Create from template
          this.templateService.createProjectFromTemplate(templateId, project).subscribe(() => {
            this.dialogRef.close(true);
          });
        } else {
          // Create normally
          this.projectService.createProject(project).subscribe(() => this.dialogRef.close(true));
        }
      }
    }
  }
}
