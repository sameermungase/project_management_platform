import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { ApprovalService, Approval } from './approval.service';
import { PermissionService } from '../../core/permission.service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApprovalDetailDialogComponent } from './approval-detail-dialog.component';

@Component({
  selector: 'app-approval-queue',
  standalone: true,
  imports: [SharedModule],
  template: `
    <div class="approval-queue-container">
      <div class="header">
        <h1>Approval Queue</h1>
        <button mat-raised-button color="accent" (click)="refreshApprovals()">
          <mat-icon>refresh</mat-icon> Refresh
        </button>
      </div>

      <mat-card class="card-container" *ngIf="approvals.length === 0">
        <p class="no-data">No pending approvals</p>
      </mat-card>

      <div class="approvals-table" *ngIf="approvals.length > 0">
        <table mat-table [dataSource]="approvals" class="mat-elevation-z8">
          <ng-container matColumnDef="entityType">
            <th mat-header-cell *matHeaderCellDef> Entity Type </th>
            <td mat-cell *matCellDef="let approval">
              <mat-chip-set>
                <mat-chip [color]="getEntityTypeColor(approval.entityType)" selected>
                  {{ getEntityTypeLabel(approval.entityType) }}
                </mat-chip>
              </mat-chip-set>
            </td>
          </ng-container>

          <ng-container matColumnDef="requestedBy">
            <th mat-header-cell *matHeaderCellDef> Requested By </th>
            <td mat-cell *matCellDef="let approval"> {{ approval.requestedBy }} </td>
          </ng-container>

          <ng-container matColumnDef="requestedAt">
            <th mat-header-cell *matHeaderCellDef> Requested At </th>
            <td mat-cell *matCellDef="let approval"> {{ approval.requestedAt | date:'short' }} </td>
          </ng-container>

          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef> Status </th>
            <td mat-cell *matCellDef="let approval">
              <mat-chip-set>
                <mat-chip [color]="getStatusColor(approval.status)" selected>
                  {{ approval.status }}
                </mat-chip>
              </mat-chip-set>
            </td>
          </ng-container>

          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef> Actions </th>
            <td mat-cell *matCellDef="let approval">
              <button mat-icon-button (click)="viewDetails(approval)" color="primary" matTooltip="View Details">
                <mat-icon>visibility</mat-icon>
              </button>
              <button mat-icon-button 
                      (click)="approveItem(approval)" 
                      color="accent" 
                      [disabled]="approval.status !== 'PENDING' || loading || !canApproveApprovals()"
                      matTooltip="Approve (SENIOR+ required)">
                <mat-icon>check_circle</mat-icon>
              </button>
              <button mat-icon-button 
                      (click)="rejectItem(approval)" 
                      color="warn" 
                      [disabled]="approval.status !== 'PENDING' || loading"
                      matTooltip="Reject">
                <mat-icon>cancel</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .approval-queue-container {
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

    .approvals-table {
      overflow: auto;
    }

    table {
      width: 100%;
      margin-top: 20px;
    }

    .mat-column-entityType {
      max-width: 120px;
    }

    .mat-column-status {
      max-width: 100px;
    }

    .mat-column-actions {
      max-width: 180px;
      text-align: center;
    }
  `]
})
export class ApprovalQueueComponent implements OnInit {
  approvals: Approval[] = [];
  displayedColumns: string[] = ['entityType', 'requestedBy', 'requestedAt', 'status', 'actions'];
  loading = false;

  constructor(
    private approvalService: ApprovalService,
    private permissionService: PermissionService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadApprovals();
  }

  loadApprovals() {
    this.approvalService.getPendingApprovals().subscribe({
      next: (data) => {
        this.approvals = data;
      },
      error: (error) => {
        this.snackBar.open('Failed to load approvals', 'Close', { duration: 3000 });
        console.error(error);
      }
    });
  }

  refreshApprovals() {
    this.loadApprovals();
  }

  viewDetails(approval: Approval) {
    const dialogRef = this.dialog.open(ApprovalDetailDialogComponent, {
      width: '600px',
      data: { approval }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadApprovals();
      }
    });
  }

  approveItem(approval: Approval) {
    this.loading = true;
    this.approvalService.approve(approval.id).subscribe({
      next: () => {
        this.snackBar.open('Approval granted', 'Close', { duration: 3000 });
        this.loading = false;
        this.loadApprovals();
      },
      error: (error) => {
        this.loading = false;
        this.snackBar.open('Failed to approve', 'Close', { duration: 3000 });
        console.error(error);
      }
    });
  }

  rejectItem(approval: Approval) {
    const reason = prompt('Enter rejection reason:');
    if (reason) {
      this.loading = true;
      this.approvalService.reject(approval.id, reason).subscribe({
        next: () => {
          this.snackBar.open('Approval rejected', 'Close', { duration: 3000 });
          this.loading = false;
          this.loadApprovals();
        },
        error: (error) => {
          this.loading = false;
          this.snackBar.open('Failed to reject', 'Close', { duration: 3000 });
          console.error(error);
        }
      });
    }
  }

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

  canApproveApprovals(): boolean {
    return this.permissionService.canApproveApprovals();
  }
}
