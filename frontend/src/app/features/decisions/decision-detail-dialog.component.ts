import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { DecisionLog } from './decision.service';

@Component({
  selector: 'app-decision-detail-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDividerModule
  ],
  template: `
    <h2 mat-dialog-title>{{ data.decision.title }}</h2>
    <mat-dialog-content>
      <mat-card class="detail-card">
        <mat-card-content>
          <div class="detail-section">
            <h3>Decision Details</h3>
            <div class="detail-row">
              <strong>Status:</strong>
              <mat-chip-set>
                <mat-chip [color]="getStatusColor(data.decision.status || '')" selected>
                  {{ data.decision.status }}
                </mat-chip>
              </mat-chip-set>
            </div>
            <div class="detail-row">
              <strong>Type:</strong>
              <span>{{ data.decision.decisionType || 'General' }}</span>
            </div>
            <div class="detail-row">
              <strong>Visibility:</strong>
              <span>{{ data.decision.visibilityLevel }}</span>
            </div>
          </div>

          <mat-divider></mat-divider>

          <div class="detail-section">
            <h3>Description</h3>
            <p class="description-text">{{ data.decision.description }}</p>
          </div>

          <mat-divider></mat-divider>

          <div class="detail-section">
            <h3>Timeline</h3>
            <div class="detail-row">
              <strong>Decided By:</strong>
              <span>{{ data.decision.decidedBy }}</span>
            </div>
            <div class="detail-row">
              <strong>Created At:</strong>
              <span>{{ data.decision.createdAt | date:'full' }}</span>
            </div>
            <div class="detail-row">
              <strong>Updated At:</strong>
              <span>{{ data.decision.updatedAt | date:'full' }}</span>
            </div>
            <div class="detail-row" *ngIf="data.decision.approvedBy">
              <strong>Approved By:</strong>
              <span>{{ data.decision.approvedBy }}</span>
            </div>
          </div>
        </mat-card-content>
      </mat-card>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Close</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .detail-card {
      margin: 0;
    }

    .detail-section {
      margin-bottom: 20px;
      padding: 15px 0;
    }

    .detail-section h3 {
      margin: 0 0 15px 0;
      font-size: 14px;
      font-weight: 600;
      color: #3f51b5;
    }

    .detail-row {
      display: flex;
      align-items: center;
      gap: 15px;
      margin-bottom: 12px;
      flex-wrap: wrap;
    }

    .detail-row strong {
      min-width: 100px;
      color: #333;
    }

    .description-text {
      margin: 0;
      padding: 15px;
      background-color: #f5f5f5;
      border-left: 3px solid #3f51b5;
      border-radius: 2px;
      line-height: 1.6;
      white-space: pre-wrap;
      word-wrap: break-word;
    }

    mat-chip-set {
      display: inline-block;
    }
  `]
})
export class DecisionDetailDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<DecisionDetailDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { decision: DecisionLog; projectId: string }
  ) {}

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
