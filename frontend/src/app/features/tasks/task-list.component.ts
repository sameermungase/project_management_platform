import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { TaskService, Task } from './task.service';
import { MatDialog } from '@angular/material/dialog';
import { TaskFormComponent } from './task-form.component';
import { PageEvent } from '@angular/material/paginator';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [SharedModule],
  template: `
    <div class="task-list-container">
      <div class="header">
        <h1>Tasks</h1>
        <div class="header-actions">
          <button mat-raised-button color="accent" [routerLink]="['/tasks/kanban']" [queryParams]="{projectId: projectId}" [disabled]="!projectId">
            <mat-icon>view_kanban</mat-icon> Kanban Board
          </button>
          <button mat-raised-button color="primary" (click)="openDialog()" [disabled]="!projectId">
            <mat-icon>add</mat-icon> New Task
          </button>
        </div>
      </div>

      <div *ngIf="!projectId" class="alert-info">
        Showing all your tasks across all projects.
      </div>

      <div class="task-board">
        <table mat-table [dataSource]="tasks" class="mat-elevation-z8">
          <ng-container matColumnDef="title">
            <th mat-header-cell *matHeaderCellDef> Title </th>
            <td mat-cell *matCellDef="let task"> 
              <a [routerLink]="['/tasks', task.id]" class="task-link">{{task.title}}</a>
            </td>
          </ng-container>

          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef> Status </th>
            <td mat-cell *matCellDef="let task"> 
                <mat-chip-set>
                    <mat-chip [color]="getStatusColor(task.status)" selected>{{task.status}}</mat-chip>
                </mat-chip-set>
            </td>
          </ng-container>

          <ng-container matColumnDef="priority">
            <th mat-header-cell *matHeaderCellDef> Priority </th>
            <td mat-cell *matCellDef="let task"> {{task.priority}} </td>
          </ng-container>

          <ng-container matColumnDef="dueDate">
            <th mat-header-cell *matHeaderCellDef> Due Date </th>
            <td mat-cell *matCellDef="let task"> {{task.dueDate | date}} </td>
          </ng-container>

          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef> Actions </th>
            <td mat-cell *matCellDef="let task">
              <button mat-icon-button [routerLink]="['/tasks', task.id]" color="primary" matTooltip="View Details">
                <mat-icon>visibility</mat-icon>
              </button>
              <button mat-icon-button color="primary" (click)="openDialog(task)" matTooltip="Edit">
                <mat-icon>edit</mat-icon>
              </button>
              <button mat-icon-button color="warn" (click)="deleteTask(task.id!)" matTooltip="Delete">
                <mat-icon>delete</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        </table>
        
        <mat-paginator [length]="totalElements"
                     [pageSize]="pageSize"
                     [pageSizeOptions]="[5, 10, 25]"
                     (page)="onPageChange($event)">
        </mat-paginator>
      </div>
    </div>
  `,
  styles: [`
    .task-list-container { padding: 20px; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    .header-actions { display: flex; gap: 10px; }
    .alert-info { padding: 15px; background-color: #d1ecf1; color: #0c5460; border-radius: 4px; margin-bottom: 20px; }
    .alert-warning { padding: 15px; background-color: #fff3cd; color: #856404; border-radius: 4px; }
    table { width: 100%; }
    .task-link { color: #3f51b5; text-decoration: none; font-weight: 500; }
    .task-link:hover { text-decoration: underline; }
  `]
})
export class TaskListComponent implements OnInit {
  tasks: Task[] = [];
  displayedColumns: string[] = ['title', 'status', 'priority', 'dueDate', 'actions'];
  projectId: string | null = null;
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;

  constructor(
    private route: ActivatedRoute,
    private taskService: TaskService,
    private dialog: MatDialog,
    private authService: AuthService
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.projectId = params['projectId'];
      this.loadTasks();
    });
  }

  loadTasks() {
    if (this.projectId) {
      // Load tasks for specific project
      this.taskService.getTasksByProject(this.projectId, this.pageIndex, this.pageSize).subscribe({
        next: (data) => {
          this.tasks = data.content;
          this.totalElements = data.totalElements;
        },
        error: (err) => {
          console.error('Error loading tasks:', err);
        }
      });
    } else if (this.authService.isAdmin()) {
      // Admin sees all tasks
      this.taskService.getAllTasksAdmin(this.pageIndex, this.pageSize).subscribe({
        next: (data) => {
          this.tasks = data.content;
          this.totalElements = data.totalElements;
        },
        error: (err) => {
          console.error('Error loading tasks:', err);
        }
      });
    } else {
      // Load all user tasks
      this.taskService.getTasks(this.pageIndex, this.pageSize).subscribe({
        next: (data) => {
          this.tasks = data.content;
          this.totalElements = data.totalElements;
        },
        error: (err) => {
          console.error('Error loading tasks:', err);
        }
      });
    }
  }

  onPageChange(event: PageEvent) {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadTasks();
  }

  openDialog(task?: Task) {
    const dialogRef = this.dialog.open(TaskFormComponent, {
      width: '500px',
      data: { task: task || {}, projectId: this.projectId }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadTasks();
      }
    });
  }

  deleteTask(id: string) {
    if (confirm('Are you sure you want to delete this task?')) {
      this.taskService.deleteTask(id).subscribe({
        next: () => this.loadTasks(),
        error: (err) => {
          console.error('Error deleting task:', err);
          alert('Failed to delete task');
        }
      });
    }
  }

  getStatusColor(status: string): string {
      switch(status) {
          case 'DONE': return 'accent';
          case 'IN_PROGRESS': return 'primary';
          default: return 'warn';
      }
  }
}
