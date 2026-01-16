import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { ProjectService, Project } from './project.service';
import { MatDialog } from '@angular/material/dialog';
import { ProjectFormComponent } from './project-form.component';
import { PageEvent } from '@angular/material/paginator';
import { ProjectMembersDialogComponent } from './project-members-dialog.component';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-project-list',
  standalone: true,
  imports: [SharedModule],
  template: `
    <div class="project-list-container">
      <div class="header">
        <h1>Projects</h1>
        <button mat-raised-button color="primary" (click)="openDialog()">
          <mat-icon>add</mat-icon> New Project
        </button>
      </div>

      <div class="grid">
        <mat-card *ngFor="let project of projects" class="project-card">
          <mat-card-header>
            <mat-card-title>{{ project.name }}</mat-card-title>
            <mat-card-subtitle>Created: {{ project.createdAt | date }}</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content>
            <p>{{ project.description }}</p>
          </mat-card-content>
          <mat-card-actions align="end">
            <button mat-icon-button matTooltip="View Tasks" color="primary" [routerLink]="['/tasks']" [queryParams]="{projectId: project.id}">
              <mat-icon>assignment</mat-icon>
            </button>
            <button mat-icon-button matTooltip="Manage Members" color="primary" (click)="openMembersDialog(project)">
              <mat-icon>people</mat-icon>
            </button>
            <button mat-icon-button matTooltip="Edit Project" color="accent" (click)="openDialog(project)">
              <mat-icon>edit</mat-icon>
            </button>
            <button mat-icon-button matTooltip="Delete Project" color="warn" (click)="deleteProject(project.id!)">
              <mat-icon>delete</mat-icon>
            </button>
          </mat-card-actions>
        </mat-card>
      </div>

      <mat-paginator [length]="totalElements"
                     [pageSize]="pageSize"
                     [pageSizeOptions]="[5, 10, 25]"
                     (page)="onPageChange($event)">
      </mat-paginator>
    </div>
  `,
  styles: [`
    .project-list-container { padding: 20px; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }
    .project-card { margin-bottom: 10px; }
  `]
})
export class ProjectListComponent implements OnInit {
  projects: Project[] = [];
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;

  constructor(private projectService: ProjectService, private dialog: MatDialog, private authService: AuthService) {}

  ngOnInit() {
    this.loadProjects();
  }

  loadProjects() {
    const projects$ = this.authService.isAdmin() 
      ? this.projectService.getAllProjectsAdmin(this.pageIndex, this.pageSize)
      : this.projectService.getProjects(this.pageIndex, this.pageSize);

    projects$.subscribe(data => {
      this.projects = data.content;
      this.totalElements = data.totalElements;
    });
  }

  onPageChange(event: PageEvent) {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadProjects();
  }

  openDialog(project?: Project) {
    const dialogRef = this.dialog.open(ProjectFormComponent, {
      width: '400px',
      data: project || {}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadProjects();
      }
    });
  }

  deleteProject(id: string) {
    if (confirm('Are you sure you want to delete this project?')) {
      this.projectService.deleteProject(id).subscribe(() => this.loadProjects());
    }
  }

  openMembersDialog(project: Project) {
    this.dialog.open(ProjectMembersDialogComponent, {
      width: '500px',
      data: { project }
    });
  }
}
