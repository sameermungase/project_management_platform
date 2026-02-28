import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { EpicService, Epic } from './epic.service';
import { MatDialog } from '@angular/material/dialog';
import { EpicFormComponent } from './epic-form.component';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-epic-list',
  standalone: true,
  imports: [SharedModule, RouterLink],
  template: `
    <div class="epic-list-container">
      <div class="header">
        <h1>Epics</h1>
        <button mat-raised-button color="primary" (click)="openDialog()">
          <mat-icon>add</mat-icon> New Epic
        </button>
      </div>

      <mat-card class="card-container" *ngIf="epics.length === 0">
        <p class="no-data">No epics yet. Create one to organize your work!</p>
      </mat-card>

      <div class="epics-grid" *ngIf="epics.length > 0">
        <mat-card *ngFor="let epic of epics" class="epic-card">
          <mat-card-header>
            <div class="epic-title">
              <h3>{{ epic.title }}</h3>
              <mat-chip-set>
                <mat-chip [color]="getStatusColor(epic.status)" selected>
                  {{ epic.status }}
                </mat-chip>
              </mat-chip-set>
            </div>
          </mat-card-header>

          <mat-card-content>
            <p class="description">{{ epic.description || 'No description provided' }}</p>
            <div class="epic-meta">
              <small>Created by: {{ epic.createdBy }}</small>
              <small>Visibility: {{ epic.visibilityLevel }}</small>
            </div>
          </mat-card-content>

          <mat-card-actions>
            <button mat-icon-button (click)="editEpic(epic)" color="primary" matTooltip="Edit">
              <mat-icon>edit</mat-icon>
            </button>
            <button mat-icon-button (click)="deleteEpic(epic.id)" color="warn" matTooltip="Delete">
              <mat-icon>delete</mat-icon>
            </button>
          </mat-card-actions>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .epic-list-container {
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

    .epics-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
      gap: 20px;
    }

    .epic-card {
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;
    }

    .epic-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
    }

    .epic-title {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
    }

    .epic-title h3 {
      margin: 0;
      flex: 1;
    }

    mat-card-header {
      margin-bottom: 15px;
    }

    mat-card-content {
      padding: 0 16px 16px 16px;
    }

    .description {
      color: #666;
      margin: 0 0 15px 0;
      line-height: 1.5;
      max-height: 60px;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .epic-meta {
      display: flex;
      flex-direction: column;
      gap: 5px;
      color: #999;
      font-size: 12px;
    }

    mat-card-actions {
      padding: 8px 16px;
      display: flex;
      gap: 8px;
    }
  `]
})
export class EpicListComponent implements OnInit {
  epics: Epic[] = [];
  projectId: string = '';

  constructor(
    private epicService: EpicService,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.projectId = params['projectId'] || '';
      if (this.projectId) {
        this.loadEpics();
      }
    });
  }

  loadEpics() {
    if (!this.projectId) return;
    
    this.epicService.getEpics(this.projectId).subscribe({
      next: (data) => {
        this.epics = data;
      },
      error: (error) => {
        this.snackBar.open('Failed to load epics', 'Close', { duration: 3000 });
        console.error(error);
      }
    });
  }

  openDialog(epic?: Epic) {
    const dialogRef = this.dialog.open(EpicFormComponent, {
      width: '500px',
      data: {
        epic: epic || { title: '', description: '', status: 'OPEN', visibilityLevel: 'TEAM' },
        projectId: this.projectId,
        isEdit: !!epic
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadEpics();
      }
    });
  }

  editEpic(epic: Epic) {
    this.openDialog(epic);
  }

  deleteEpic(epicId: string) {
    if (confirm('Are you sure you want to delete this epic?')) {
      this.epicService.deleteEpic(this.projectId, epicId).subscribe({
        next: () => {
          this.snackBar.open('Epic deleted successfully', 'Close', { duration: 3000 });
          this.loadEpics();
        },
        error: (error) => {
          this.snackBar.open('Failed to delete epic', 'Close', { duration: 3000 });
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
