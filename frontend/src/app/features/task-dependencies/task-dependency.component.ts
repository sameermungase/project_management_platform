import { Component, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { TaskDependencyService, TaskDependency, TaskDependencyRequest } from './task-dependency.service';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-task-dependencies',
  standalone: true,
  imports: [SharedModule, CommonModule],
  template: `
    <div class="dependencies-section">
      <div class="section-header">
        <h3>
          <mat-icon>account_tree</mat-icon>
          Task Dependencies
        </h3>
        <button mat-raised-button color="primary" (click)="openAddDependencyDialog()">
          <mat-icon>add</mat-icon> Add Dependency
        </button>
      </div>

      <div class="dependencies-container">
        <!-- Dependencies (This task depends on) -->
        <div class="dependency-group" *ngIf="dependencies.length > 0">
          <h4>
            <mat-icon>arrow_downward</mat-icon>
            Depends On ({{ dependencies.length }})
          </h4>
          <mat-card *ngFor="let dep of dependencies" class="dependency-card">
            <mat-card-content>
              <div class="dependency-info">
                <mat-icon class="dependency-icon">task</mat-icon>
                <div class="dependency-details">
                  <strong>{{ dep.dependsOnTaskTitle }}</strong>
                  <span class="dependency-type">{{ dep.dependencyType }}</span>
                </div>
                <button mat-icon-button color="warn" (click)="deleteDependency(dep.id!)">
                  <mat-icon>delete</mat-icon>
                </button>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Dependent Tasks (Tasks that depend on this) -->
        <div class="dependency-group" *ngIf="dependentTasks.length > 0">
          <h4>
            <mat-icon>arrow_upward</mat-icon>
            Blocks ({{ dependentTasks.length }})
          </h4>
          <mat-card *ngFor="let dep of dependentTasks" class="dependency-card">
            <mat-card-content>
              <div class="dependency-info">
                <mat-icon class="dependency-icon">task</mat-icon>
                <div class="dependency-details">
                  <strong>{{ dep.taskTitle }}</strong>
                  <span class="dependency-type">{{ dep.dependencyType }}</span>
                </div>
                <button mat-icon-button color="warn" (click)="deleteDependency(dep.id!)">
                  <mat-icon>delete</mat-icon>
                </button>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <div *ngIf="dependencies.length === 0 && dependentTasks.length === 0" class="no-dependencies">
          <mat-icon>account_tree</mat-icon>
          <p>No dependencies yet. Add dependencies to track task relationships.</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dependencies-section { padding: 20px; }
    .section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    .section-header h3 { display: flex; align-items: center; gap: 10px; }
    .dependencies-container { display: flex; flex-direction: column; gap: 20px; }
    .dependency-group h4 { display: flex; align-items: center; gap: 8px; margin-bottom: 15px; color: #666; }
    .dependency-card { margin-bottom: 10px; }
    .dependency-info { display: flex; align-items: center; gap: 15px; }
    .dependency-icon { color: #3f51b5; }
    .dependency-details { flex: 1; display: flex; flex-direction: column; gap: 5px; }
    .dependency-type { font-size: 0.85em; color: #999; text-transform: uppercase; }
    .no-dependencies { text-align: center; padding: 40px; color: #999; }
    .no-dependencies mat-icon { font-size: 48px; width: 48px; height: 48px; margin-bottom: 10px; }
  `]
})
export class TaskDependencyComponent implements OnInit {
  @Input() taskId!: string;
  @Input() availableTasks: any[] = [];

  dependencies: TaskDependency[] = [];
  dependentTasks: TaskDependency[] = [];

  constructor(
    private dependencyService: TaskDependencyService,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.loadDependencies();
  }

  loadDependencies() {
    this.dependencyService.getDependencies(this.taskId).subscribe(data => {
      this.dependencies = data;
    });
    this.dependencyService.getDependentTasks(this.taskId).subscribe(data => {
      this.dependentTasks = data;
    });
  }

  openAddDependencyDialog() {
    // Simple prompt for now - can be enhanced with a proper dialog
    const dependsOnTaskId = prompt('Enter the ID of the task this depends on:');
    if (dependsOnTaskId) {
      const request: TaskDependencyRequest = {
        taskId: this.taskId,
        dependsOnTaskId: dependsOnTaskId,
        dependencyType: 'BLOCKS'
      };
      this.dependencyService.createDependency(request).subscribe(() => {
        this.loadDependencies();
      }, error => {
        alert('Error creating dependency: ' + (error.error?.message || 'Unknown error'));
      });
    }
  }

  deleteDependency(dependencyId: string) {
    if (confirm('Are you sure you want to remove this dependency?')) {
      this.dependencyService.deleteDependency(dependencyId).subscribe(() => {
        this.loadDependencies();
      });
    }
  }
}
