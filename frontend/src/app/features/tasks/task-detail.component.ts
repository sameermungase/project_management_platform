import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { TaskService, Task } from './task.service';
import { CommentListComponent } from '../comments/comment-list.component';
import { TaskDependencyComponent } from '../task-dependencies/task-dependency.component';
import { MatDialog } from '@angular/material/dialog';
import { TaskFormComponent } from './task-form.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [SharedModule, CommonModule, CommentListComponent, TaskDependencyComponent],
  template: `
    <div class="task-detail-container">
      <div class="task-header">
        <button mat-icon-button (click)="goBack()">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <h1>{{ task?.title }}</h1>
        <div class="header-actions">
          <button mat-raised-button color="accent" (click)="editTask()">
            <mat-icon>edit</mat-icon> Edit
          </button>
          <button mat-raised-button color="warn" (click)="deleteTask()">
            <mat-icon>delete</mat-icon> Delete
          </button>
        </div>
      </div>

      <div *ngIf="loading" class="loading-container">
        <mat-spinner></mat-spinner>
      </div>

      <div *ngIf="!loading && task" class="task-content">
        <div class="task-main">
          <mat-card class="task-info-card">
            <mat-card-content>
              <div class="task-meta">
                <div class="meta-item">
                  <mat-icon>flag</mat-icon>
                  <span><strong>Priority:</strong> {{ task.priority }}</span>
                </div>
                <div class="meta-item">
                  <mat-icon>check_circle</mat-icon>
                  <span><strong>Status:</strong> {{ task.status }}</span>
                </div>
                <div class="meta-item" *ngIf="task.dueDate">
                  <mat-icon>event</mat-icon>
                  <span><strong>Due Date:</strong> {{ task.dueDate | date:'short' }}</span>
                </div>
              </div>
              <div class="task-description">
                <h3>Description</h3>
                <p>{{ task.description || 'No description provided' }}</p>
              </div>
            </mat-card-content>
          </mat-card>

          <!-- Task Dependencies -->
          <app-task-dependencies 
            *ngIf="task.id" 
            [taskId]="task.id"
            [availableTasks]="availableTasks">
          </app-task-dependencies>

          <!-- Comments -->
          <app-comment-list 
            *ngIf="task.id" 
            [taskId]="task.id">
          </app-comment-list>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .task-detail-container { padding: 20px; max-width: 1200px; margin: 0 auto; }
    .task-header { display: flex; align-items: center; gap: 15px; margin-bottom: 30px; }
    .task-header h1 { flex: 1; margin: 0; }
    .header-actions { display: flex; gap: 10px; }
    .loading-container { display: flex; justify-content: center; padding: 50px; }
    .task-content { display: flex; flex-direction: column; gap: 20px; }
    .task-main { display: flex; flex-direction: column; gap: 20px; }
    .task-info-card { margin-bottom: 20px; }
    .task-meta { display: flex; flex-wrap: wrap; gap: 20px; margin-bottom: 20px; }
    .meta-item { display: flex; align-items: center; gap: 8px; }
    .task-description h3 { margin-bottom: 10px; }
    .task-description p { line-height: 1.6; color: #666; }
  `]
})
export class TaskDetailComponent implements OnInit {
  task: Task | null = null;
  taskId: string | null = null;
  loading = true;
  availableTasks: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private taskService: TaskService,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.route.params.subscribe(params => {
      this.taskId = params['id'];
      if (this.taskId) {
        this.loadTask();
      }
    });
  }

  loadTask() {
    if (!this.taskId) return;
    
    this.loading = true;
    this.taskService.getTaskById(this.taskId).subscribe(
      (task) => {
        this.task = task;
        this.loading = false;
      },
      (error) => {
        console.error('Error loading task:', error);
        this.loading = false;
      }
    );
  }

  editTask() {
    if (!this.task) return;
    
    const dialogRef = this.dialog.open(TaskFormComponent, {
      width: '500px',
      data: { task: this.task, projectId: this.task.projectId }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadTask();
      }
    });
  }

  deleteTask() {
    if (!this.task?.id) return;
    
    if (confirm('Are you sure you want to delete this task?')) {
      this.taskService.deleteTask(this.task.id).subscribe({
        next: () => this.router.navigate(['/tasks']),
        error: (err) => {
          console.error('Error deleting task:', err);
          alert('Failed to delete task');
        }
      });
    }
  }

  goBack() {
    this.router.navigate(['/tasks']);
  }
}
