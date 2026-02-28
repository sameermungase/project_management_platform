import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { TaskService, Task } from './task.service';
import { MatDialog } from '@angular/material/dialog';
import { TaskFormComponent } from './task-form.component';
import { CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';

@Component({
  selector: 'app-task-kanban',
  standalone: true,
  imports: [SharedModule],
  template: `
    <div class="kanban-container">
      <div class="header">
        <h1>Kanban Board</h1>
        <div class="header-actions">
          <button mat-raised-button color="accent" [routerLink]="['/tasks']" [queryParams]="{projectId: projectId}" [disabled]="!projectId">
            <mat-icon>view_list</mat-icon> List View
          </button>
          <button mat-raised-button color="primary" (click)="openDialog()" [disabled]="!projectId">
            <mat-icon>add</mat-icon> New Task
          </button>
        </div>
      </div>

      <div *ngIf="!projectId" class="alert-warning">
        Please select a project from the Projects page to view tasks.
      </div>

      <div class="kanban-board" *ngIf="projectId">
        <div class="kanban-column" *ngFor="let status of statuses">
          <div class="column-header" [ngClass]="'status-' + status.key.toLowerCase()">
            <h2>{{ status.label }}</h2>
            <span class="task-count">{{ getTasksByStatus(status.key).length }}</span>
          </div>
          
          <div 
            cdkDropList 
            [cdkDropListData]="getTasksByStatus(status.key)"
            [id]="status.key"
            [cdkDropListConnectedTo]="getConnectedLists()"
            (cdkDropListDropped)="onTaskDrop($event)"
            class="task-list">
            
            <div 
              *ngFor="let task of getTasksByStatus(status.key)" 
              cdkDrag
              class="task-card">
              <mat-card>
                <mat-card-header>
                  <mat-card-title>{{ task.title }}</mat-card-title>
                  <div class="card-actions">
                    <button mat-icon-button (click)="openDialog(task)" cdkDragHandle>
                      <mat-icon>edit</mat-icon>
                    </button>
                    <button mat-icon-button color="warn" (click)="deleteTask(task.id!)">
                      <mat-icon>delete</mat-icon>
                    </button>
                  </div>
                </mat-card-header>
                <mat-card-content>
                  <p class="description">{{ task.description }}</p>
                  <div class="task-meta">
                    <mat-chip-set>
                      <mat-chip [color]="getPriorityColor(task.priority)" selected>
                        <mat-icon>flag</mat-icon>
                        {{ task.priority }}
                      </mat-chip>
                    </mat-chip-set>
                    <div class="due-date" *ngIf="task.dueDate">
                      <mat-icon>event</mat-icon>
                      <span>{{ task.dueDate | date:'short' }}</span>
                    </div>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>
            
            <div *ngIf="getTasksByStatus(status.key).length === 0" class="empty-state">
              <mat-icon>inbox</mat-icon>
              <p>No tasks</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styleUrl: './task-kanban.component.scss'
})
export class TaskKanbanComponent implements OnInit {
  tasks: Task[] = [];
  projectId: string | null = null;
  
  statuses = [
    { key: 'TO_DO', label: 'To Do' },
    { key: 'IN_PROGRESS', label: 'In Progress' },
    { key: 'DONE', label: 'Done' }
  ];

  constructor(
    private route: ActivatedRoute,
    private taskService: TaskService,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.projectId = params['projectId'];
      if (this.projectId) {
        this.loadTasks();
      }
    });
  }

  loadTasks() {
    if (this.projectId) {
      // Load all tasks without pagination for Kanban view
      this.taskService.getTasksByProject(this.projectId, 0, 1000).subscribe({
        next: (data) => {
          this.tasks = data.content;
        },
        error: (err) => {
          console.error('Error loading tasks:', err);
        }
      });
    }
  }

  getTasksByStatus(status: string): Task[] {
    return this.tasks.filter(task => task.status === status);
  }

  getConnectedLists(): string[] {
    return this.statuses.map(s => s.key);
  }

  onTaskDrop(event: CdkDragDrop<Task[]>) {
    if (event.previousContainer === event.container) {
      // Same column - just reorder
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      // Different column - transfer and update status
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );
      
      // Update task status
      const task = event.container.data[event.currentIndex];
      const newStatus = event.container.id as 'TO_DO' | 'IN_PROGRESS' | 'DONE';
      const updatedTask = { ...task, status: newStatus };
      
      this.taskService.updateTask(task.id!, updatedTask).subscribe({
        next: () => {
          this.loadTasks();
        },
        error: (err) => {
          console.error('Error updating task:', err);
        }
      });
    }
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

  getPriorityColor(priority: string): string {
    switch(priority) {
      case 'HIGH': return 'warn';
      case 'MEDIUM': return 'accent';
      default: return 'primary';
    }
  }
}
