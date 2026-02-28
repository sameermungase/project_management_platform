import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { DecisionService, DecisionLog } from './decision.service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DecisionDetailDialogComponent } from './decision-detail-dialog.component';

@Component({
  selector: 'app-decision-log',
  standalone: true,
  imports: [SharedModule],
  template: `
    <div class="decision-log-container">
      <div class="header">
        <h1>Decision Log</h1>
        <div class="header-actions">
          <button mat-raised-button color="primary" (click)="refreshDecisions()">
            <mat-icon>refresh</mat-icon> Refresh
          </button>
        </div>
      </div>

      <mat-card class="card-container" *ngIf="decisions.length === 0">
        <p class="no-data">No decisions logged yet</p>
      </mat-card>

      <div class="decisions-grid" *ngIf="decisions.length > 0">
        <mat-card *ngFor="let decision of decisions" class="decision-card">
          <mat-card-header>
            <div class="decision-title">
              <h3>{{ decision.title }}</h3>
              <mat-chip-set>
                <mat-chip [color]="getStatusColor(decision.status)" selected>
                  {{ decision.status }}
                </mat-chip>
              </mat-chip-set>
            </div>
          </mat-card-header>

          <mat-card-content>
            <p class="description">{{ decision.description }}</p>
            <div class="decision-meta">
              <div class="meta-row">
                <small><strong>Type:</strong> {{ decision.decisionType || 'General' }}</small>
              </div>
              <div class="meta-row">
                <small><strong>Decided By:</strong> {{ decision.decidedBy }}</small>
              </div>
              <div class="meta-row">
                <small><strong>Visibility:</strong> {{ decision.visibilityLevel }}</small>
              </div>
              <div class="meta-row">
                <small><strong>Created:</strong> {{ decision.createdAt | date:'short' }}</small>
              </div>
              <div class="meta-row" *ngIf="decision.approvedBy">
                <small><strong>Approved By:</strong> {{ decision.approvedBy }}</small>
              </div>
            </div>
          </mat-card-content>

          <mat-card-actions>
            <button mat-icon-button (click)="viewDetails(decision)" color="primary" matTooltip="View Details">
              <mat-icon>visibility</mat-icon>
            </button>
            <button mat-icon-button 
                    (click)="approveDecision(decision)" 
                    color="accent" 
                    [disabled]="decision.status !== 'PROPOSED' || loading"
                    matTooltip="Approve">
              <mat-icon>check_circle</mat-icon>
            </button>
          </mat-card-actions>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .decision-log-container {
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

    .header-actions {
      display: flex;
      gap: 10px;
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

    .decisions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
      gap: 20px;
    }

    .decision-card {
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;
    }

    .decision-card:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
    }

    .decision-title {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
    }

    .decision-title h3 {
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

    .decision-meta {
      display: flex;
      flex-direction: column;
      gap: 8px;
      color: #999;
      font-size: 12px;
    }

    .meta-row {
      display: flex;
      gap: 5px;
    }

    mat-card-actions {
      padding: 8px 16px;
      display: flex;
      gap: 8px;
    }
  `]
})
export class DecisionLogComponent implements OnInit {
  decisions: DecisionLog[] = [];
  projectId: string = '';
  loading = false;

  constructor(
    private decisionService: DecisionService,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.projectId = params['projectId'] || '';
      if (this.projectId) {
        this.loadDecisions();
      }
    });
  }

  loadDecisions() {
    if (!this.projectId) return;

    this.decisionService.getDecisions(this.projectId).subscribe({
      next: (data) => {
        this.decisions = data;
      },
      error: (error) => {
        this.snackBar.open('Failed to load decisions', 'Close', { duration: 3000 });
        console.error(error);
      }
    });
  }

  refreshDecisions() {
    this.loadDecisions();
  }

  viewDetails(decision: DecisionLog) {
    const dialogRef = this.dialog.open(DecisionDetailDialogComponent, {
      width: '700px',
      data: { decision, projectId: this.projectId }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadDecisions();
      }
    });
  }

  approveDecision(decision: DecisionLog) {
    if (confirm('Are you sure you want to approve this decision?')) {
      this.loading = true;
      this.decisionService.approveDecision(this.projectId, decision.id).subscribe({
        next: () => {
          this.snackBar.open('Decision approved', 'Close', { duration: 3000 });
          this.loading = false;
          this.loadDecisions();
        },
        error: (error) => {
          this.loading = false;
          this.snackBar.open('Failed to approve decision', 'Close', { duration: 3000 });
          console.error(error);
        }
      });
    }
  }

  getStatusColor(status: string): 'primary' | 'accent' | 'warn' {
    switch (status) {
      case 'PROPOSED':
        return 'accent';
      case 'APPROVED':
        return 'primary';
      case 'REJECTED':
        return 'warn';
      default:
        return 'primary';
    }
  }
}
