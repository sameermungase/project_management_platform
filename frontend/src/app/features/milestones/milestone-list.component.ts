import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { MilestoneService, Milestone } from './milestone.service';
import { MatDialog } from '@angular/material/dialog';
import { MilestoneFormComponent } from './milestone-form.component';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-milestone-list',
  standalone: true,
  imports: [SharedModule],
  template: `
    <div class="milestone-list-container">
      <div class="header">
        <h1>Milestones</h1>
        <button mat-raised-button color="primary" (click)="openDialog()">
          <mat-icon>add</mat-icon> New Milestone
        </button>
      </div>

      <mat-card class="card-container" *ngIf="milestones.length === 0">
        <p class="no-data">No milestones yet. Create one to track project progress!</p>
      </mat-card>

      <div class="milestones-list" *ngIf="milestones.length > 0">
        <mat-expansion-panel *ngFor="let milestone of milestones" class="milestone-panel">
          <mat-expansion-panel-header>
            <mat-panel-title class="milestone-header">
              <span class="milestone-title">{{ milestone.title }}</span>
              <mat-chip-set>
                <mat-chip [color]="getStatusColor(milestone.status)" selected>
                  {{ milestone.status }}
                </mat-chip>
              </mat-chip-set>
            </mat-panel-title>
            <mat-panel-description class="milestone-date">
              {{ milestone.targetDate ? (milestone.targetDate | date:'short') : 'No target date' }}
            </mat-panel-description>
          </mat-expansion-panel-header>

          <mat-card-content class="milestone-content">
            <p class="description">{{ milestone.description || 'No description provided' }}</p>
            <div class="milestone-meta">
              <small *ngIf="milestone.epicId">Epic ID: {{ milestone.epicId }}</small>
              <small>Created by: {{ milestone.createdBy }}</small>
            </div>
          </mat-card-content>

          <mat-divider></mat-divider>

          <mat-card-actions class="milestone-actions">
            <button mat-icon-button (click)="editMilestone(milestone)" color="primary" matTooltip="Edit">
              <mat-icon>edit</mat-icon>
            </button>
            <button mat-icon-button (click)="deleteMilestone(milestone.id)" color="warn" matTooltip="Delete">
              <mat-icon>delete</mat-icon>
            </button>
          </mat-card-actions>
        </mat-expansion-panel>
      </div>
    </div>
  `,
  styles: [`
    .milestone-list-container {
      padding: 20px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 30px;
    }

    .header h1 {
      margin: 0;
    }

    .card-container {
      padding: 40px;
      text-align: center;
    }

    .no-data {
      color: #999;
      font-size: 16px;
      margin: 0;
    }

    .milestones-list {
      max-width: 900px;
      margin: 0 auto;
    }

    .milestone-panel {
      margin-bottom: 15px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    }

    .milestone-header {
      display: flex;
      align-items: center;
      gap: 15px;
      flex: 1;
      width: 100%;
    }

    .milestone-title {
      font-weight: 500;
      flex: 1;
    }

    .milestone-date {
      color: #999;
      font-size: 12px;
    }

    .milestone-content {
      padding: 16px;
    }

    .description {
      color: #666;
      margin: 0 0 15px 0;
      line-height: 1.5;
    }

    .milestone-meta {
      display: flex;
      flex-direction: column;
      gap: 5px;
      color: #999;
      font-size: 12px;
    }

    .milestone-actions {
      padding: 8px 16px;
      display: flex;
      gap: 8px;
      justify-content: flex-end;
    }

    mat-divider {
      margin: 0;
    }
  `]
})
export class MilestoneListComponent implements OnInit {
  milestones: Milestone[] = [];
  projectId: string = '';

  constructor(
    private milestoneService: MilestoneService,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.projectId = params['projectId'] || '';
      if (this.projectId) {
        this.loadMilestones();
      }
    });
  }

  loadMilestones() {
    if (!this.projectId) return;
    
    this.milestoneService.getMilestones(this.projectId).subscribe({
      next: (data) => {
        this.milestones = data;
      },
      error: (error) => {
        this.snackBar.open('Failed to load milestones', 'Close', { duration: 3000 });
        console.error(error);
      }
    });
  }

  openDialog(milestone?: Milestone) {
    const dialogRef = this.dialog.open(MilestoneFormComponent, {
      width: '500px',
      data: {
        milestone: milestone || { title: '', description: '', status: 'OPEN' },
        projectId: this.projectId,
        isEdit: !!milestone
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadMilestones();
      }
    });
  }

  editMilestone(milestone: Milestone) {
    this.openDialog(milestone);
  }

  deleteMilestone(milestoneId: string) {
    if (confirm('Are you sure you want to delete this milestone?')) {
      this.milestoneService.deleteMilestone(this.projectId, milestoneId).subscribe({
        next: () => {
          this.snackBar.open('Milestone deleted successfully', 'Close', { duration: 3000 });
          this.loadMilestones();
        },
        error: (error) => {
          this.snackBar.open('Failed to delete milestone', 'Close', { duration: 3000 });
          console.error(error);
        }
      });
    }
  }

  getStatusColor(status: string): 'primary' | 'accent' | 'warn' {
    switch (status) {
      case 'OPEN':
        return 'primary';
      case 'IN_PROGRESS':
        return 'accent';
      case 'CLOSED':
        return 'warn';
      default:
        return 'primary';
    }
  }
}
