import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { TemplateService, ProjectTemplate } from './template.service';
import { Router } from '@angular/router';
import { ProjectService } from '../projects/project.service';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-template-selector',
  standalone: true,
  imports: [SharedModule, CommonModule],
  template: `
    <div class="template-selector">
      <div class="header">
        <h2>
          <mat-icon>description</mat-icon>
          Project Templates
        </h2>
        <button mat-raised-button color="primary" (click)="showCreateTemplate = !showCreateTemplate">
          <mat-icon>add</mat-icon> Create Template
        </button>
      </div>

      <div *ngIf="showCreateTemplate" class="create-template-form">
        <h3>Create New Template</h3>
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Template Name</mat-label>
          <input matInput [(ngModel)]="newTemplate.name" required>
        </mat-form-field>
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Description</mat-label>
          <textarea matInput [(ngModel)]="newTemplate.description" rows="3"></textarea>
        </mat-form-field>
        <mat-form-field appearance="outline">
          <mat-label>Template Type</mat-label>
          <mat-select [(ngModel)]="newTemplate.templateType">
            <mat-option value="AGILE">Agile</mat-option>
            <mat-option value="WATERFALL">Waterfall</mat-option>
            <mat-option value="MARKETING">Marketing</mat-option>
            <mat-option value="CUSTOM">Custom</mat-option>
          </mat-select>
        </mat-form-field>
        <mat-checkbox [(ngModel)]="newTemplate.isPublic">Make Public</mat-checkbox>
        <div class="form-actions">
          <button mat-button (click)="showCreateTemplate = false">Cancel</button>
          <button mat-raised-button color="primary" (click)="createTemplate()">Create Template</button>
        </div>
      </div>

      <div class="templates-grid">
        <mat-card *ngFor="let template of templates" class="template-card">
          <mat-card-header>
            <mat-card-title>{{ template.name }}</mat-card-title>
            <mat-card-subtitle>{{ template.templateType }}</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content>
            <p>{{ template.description || 'No description' }}</p>
            <div class="template-info">
              <span *ngIf="template.templateTasks">
                <mat-icon>task</mat-icon>
                {{ template.templateTasks.length }} tasks
              </span>
              <span *ngIf="template.isPublic" class="public-badge">
                <mat-icon>public</mat-icon> Public
              </span>
            </div>
          </mat-card-content>
          <mat-card-actions>
            <button mat-raised-button color="primary" (click)="useTemplate(template)">
              <mat-icon>play_arrow</mat-icon> Use Template
            </button>
            <button mat-icon-button (click)="viewTemplate(template)">
              <mat-icon>visibility</mat-icon>
            </button>
            <button mat-icon-button color="warn" *ngIf="canDelete(template)" (click)="deleteTemplate(template.id!)">
              <mat-icon>delete</mat-icon>
            </button>
          </mat-card-actions>
        </mat-card>
      </div>

      <div *ngIf="templates.length === 0" class="no-templates">
        <mat-icon>description</mat-icon>
        <p>No templates available. Create one to get started!</p>
      </div>
    </div>
  `,
  styles: [`
    .template-selector { padding: 20px; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    .header h2 { display: flex; align-items: center; gap: 10px; }
    .create-template-form { padding: 20px; background: #f5f5f5; border-radius: 8px; margin-bottom: 20px; }
    .full-width { width: 100%; }
    .form-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 15px; }
    .templates-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }
    .template-card { height: 100%; }
    .template-info { display: flex; gap: 15px; margin-top: 10px; color: #666; }
    .template-info span { display: flex; align-items: center; gap: 5px; }
    .public-badge { color: #4caf50; }
    .no-templates { text-align: center; padding: 60px; color: #999; }
    .no-templates mat-icon { font-size: 64px; width: 64px; height: 64px; margin-bottom: 15px; }
  `]
})
export class TemplateSelectorComponent implements OnInit {
  templates: ProjectTemplate[] = [];
  showCreateTemplate = false;
  newTemplate: any = {
    name: '',
    description: '',
    templateType: 'CUSTOM',
    isPublic: false,
    templateTasks: []
  };

  constructor(
    private templateService: TemplateService,
    private projectService: ProjectService,
    private router: Router,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.loadTemplates();
  }

  loadTemplates() {
    this.templateService.getAllTemplates().subscribe({
      next: (data) => {
        this.templates = data;
      },
      error: (err) => {
        console.error('Error loading templates:', err);
      }
    });
  }

  createTemplate() {
    this.templateService.createTemplate(this.newTemplate).subscribe({
      next: () => {
        this.showCreateTemplate = false;
        this.newTemplate = { name: '', description: '', templateType: 'CUSTOM', isPublic: false, templateTasks: [] };
        this.loadTemplates();
      },
      error: (err) => {
        console.error('Error creating template:', err);
      }
    });
  }

  useTemplate(template: ProjectTemplate) {
    const projectName = prompt('Enter project name:');
    if (projectName) {
      this.templateService.createProjectFromTemplate(template.id!, { name: projectName }).subscribe({
        next: (project) => {
          this.router.navigate(['/projects']);
        },
        error: (err) => {
          console.error('Error creating project from template:', err);
          alert('Failed to create project from template');
        }
      });
    }
  }

  viewTemplate(template: ProjectTemplate) {
    // Show template details in a dialog
    alert(`Template: ${template.name}\nTasks: ${template.templateTasks?.length || 0}`);
  }

  deleteTemplate(templateId: string) {
    if (confirm('Are you sure you want to delete this template?')) {
      this.templateService.deleteTemplate(templateId).subscribe({
        next: () => {
          this.loadTemplates();
        },
        error: (err) => {
          console.error('Error deleting template:', err);
          alert('Failed to delete template');
        }
      });
    }
  }

  canDelete(template: ProjectTemplate): boolean {
    // Add logic to check if user can delete (e.g., is owner)
    return true; // Simplified for now
  }
}
