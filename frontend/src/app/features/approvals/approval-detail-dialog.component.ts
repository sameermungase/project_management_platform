import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { Approval } from './approval.service';

@Component({
  selector: 'app-approval-detail-dialog',
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
    <h2 mat-dialog-title>Approval Details</h2>
    <mat-dialog-content>
      <mat-card class="detail-card">
        <mat-card-content>
          <div class="detail-section">
            <h3>Request Information</h3>
            <div class="detail-row">
              <strong>Entity Type:</strong>
              <mat-chip-set>
                <mat-chip [color]="getEntityTypeColor(data.approval.entityType || '')" selected>
                  {{ getEntityTypeLabel(data.approval.entityType || '') }}
                </mat-chip>
              </mat-chip-set>
            </div>
            <div class="detail-row">
              <strong>Entity ID:</strong>
              <code>{{ data.approval.entityId }}</code>
            </div>
            <div class="detail-row">
              <strong>Requested By:</strong>
              <span>{{ data.approval.requestedBy }}</span>
            </div>
            <div class="detail-row">
              <strong>Requested At:</strong>
              <span>{{ data.approval.requestedAt | date:'full' }}</span>
            </div>
          </div>

          <mat-divider></mat-divider>

          <div class="detail-section">
            <h3>Status</h3>
            <div class="detail-row">
              <strong>Status:</strong>
              <mat-chip-set>
                <mat-chip [color]="getStatusColor(data.approval.status || '')" selected>
                  {{ data.approval.status }}
                </mat-chip>
              </mat-chip-set>
            </div>
            <div class="detail-row" *ngIf="data.approval.approvedBy">
              <strong>Approved By:</strong>
              <span>{{ data.approval.approvedBy }}</span>
            </div>
            <div class="detail-row" *ngIf="data.approval.approvedAt">
              <strong>Approved At:</strong>
              <span>{{ data.approval.approvedAt | date:'full' }}</span>
            </div>
            <div class="detail-row" *ngIf="data.approval.rejectionReason">
              <strong>Rejection Reason:</strong>
              <p class="reason-text">{{ data.approval.rejectionReason }}</p>
            </div>
          </div>

          <mat-divider></mat-divider>

          <div class="detail-section" *ngIf="data.approval.comments">
            <h3>Comments</h3>
            <p class="comments-text">{{ data.approval.comments }}</p>
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
      min-width: 120px;
      color: #333;
    }

    code {
      background-color: #f5f5f5;
      padding: 2px 6px;
      border-radius: 3px;
      font-family: monospace;
      font-size: 12px;
    }

    .reason-text, .comments-text {
      margin: 0;
      padding: 10px;
      background-color: #f5f5f5;
      border-left: 3px solid #ff9800;
      border-radius: 2px;
      line-height: 1.5;
    }

    .comments-text {
      border-left-color: #3f51b5;
    }

    mat-chip-set {
      display: inline-block;
    }
  `]
})
export class ApprovalDetailDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ApprovalDetailDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { approval: Approval }
  ) {}

  getEntityTypeLabel(type: string): string {
    const labels: { [key: string]: string } = {
      TASK: 'Task',
      EPIC: 'Epic',
      MILESTONE: 'Milestone',
      DESIGN_DECISION: 'Design Decision',
      ARCHITECTURE_CHANGE: 'Architecture Change'
    };
    return labels[type] || type;
  }

  getEntityTypeColor(type: string): 'primary' | 'accent' | 'warn' {
    const colors: { [key: string]: any } = {
      TASK: 'primary',
      EPIC: 'accent',
      MILESTONE: 'primary',
      DESIGN_DECISION: 'accent',
      ARCHITECTURE_CHANGE: 'warn'
    };
    return colors[type] || 'primary';
  }

  getStatusColor(status: string): 'primary' | 'accent' | 'warn' {
    switch (status) {
      case 'PENDING':
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
