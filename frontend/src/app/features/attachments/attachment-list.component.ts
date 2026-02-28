import { Component, Input, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatConfirmDialogComponent } from './mat-confirm-dialog.component';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { AttachmentService, AttachmentDTO } from './attachment.service';

@Component({
  selector: 'app-attachment-list',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatDialogModule
  ],
  template: `
    <div class="attachments-container">
      <h3 class="section-title">Attachments</h3>
      
      <div *ngIf="isLoading" class="loading-container">
        <mat-spinner diameter="40"></mat-spinner>
        <p>Loading attachments...</p>
      </div>
      
      <mat-list *ngIf="!isLoading && attachments.length > 0" class="attachments-list">
        <mat-list-item *ngFor="let attachment of attachments" class="attachment-item">
          <mat-icon matListItemIcon [class]="'file-icon'">
            {{ getFileIcon(attachment.mimeType) }}
          </mat-icon>
          <div matListItemTitle class="attachment-title">
            <a href="javascript:void(0)" 
               (click)="downloadAttachment(attachment)"
               class="filename-link"
               [title]="attachment.originalFilename">
              {{ attachment.originalFilename }}
            </a>
            <span class="file-size">({{ formatFileSize(attachment.fileSize) }})</span>
          </div>
          <div matListItemLine class="attachment-meta">
            <span class="uploader">{{ attachment.createdByName }}</span>
            <span class="divider">•</span>
            <span class="date">{{ formatDate(attachment.createdAt) }}</span>
          </div>
          <button mat-icon-button
                  matListItemMeta
                  [matMenuTriggerFor]="menu"
                  class="action-menu">
            <mat-icon>more_vert</mat-icon>
          </button>
          
          <mat-menu #menu="matMenu">
            <button mat-menu-item (click)="downloadAttachment(attachment)">
              <mat-icon>download</mat-icon>
              <span>Download</span>
            </button>
            <button mat-menu-item (click)="deleteAttachment(attachment)" class="delete-option">
              <mat-icon>delete</mat-icon>
              <span>Delete</span>
            </button>
          </mat-menu>
        </mat-list-item>
      </mat-list>
      
      <div *ngIf="!isLoading && attachments.length === 0" class="empty-state">
        <mat-icon>attach_file</mat-icon>
        <p>No attachments yet</p>
      </div>
    </div>
  `,
  styles: [`
    .attachments-container {
      margin-top: 24px;
    }
    
    .section-title {
      margin: 0 0 16px 0;
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
    
    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px;
      text-align: center;
    }
    
    .attachments-list {
      background-color: transparent;
      padding: 0;
    }
    
    .attachment-item {
      background-color: white;
      margin-bottom: 8px;
      border-radius: 4px;
      border: 1px solid #e0e0e0;
      padding: 12px 16px;
    }
    
    .attachment-item:hover {
      background-color: #fafafa;
    }
    
    .file-icon {
      color: #2196f3;
      margin-right: 12px;
    }
    
    .attachment-title {
      margin-bottom: 4px;
    }
    
    .filename-link {
      color: #2196f3;
      text-decoration: none;
      font-weight: 500;
      word-break: break-word;
      cursor: pointer;
    }
    
    .filename-link:hover {
      text-decoration: underline;
    }
    
    .file-size {
      color: #999;
      font-size: 12px;
      margin-left: 8px;
    }
    
    .attachment-meta {
      font-size: 12px;
      color: #999;
    }
    
    .uploader {
      font-weight: 500;
      color: #666;
    }
    
    .divider {
      margin: 0 8px;
    }
    
    .action-menu {
      color: #999;
    }
    
    .delete-option {
      color: #d32f2f;
    }
    
    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px 20px;
      text-align: center;
      color: #999;
    }
    
    .empty-state mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 12px;
      color: #ccc;
    }
  `]
})
export class AttachmentListComponent implements OnInit, OnChanges {
  @Input() taskId?: string;
  @Input() epicId?: string;
  
  attachments: AttachmentDTO[] = [];
  isLoading = false;
  
  constructor(
    private attachmentService: AttachmentService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}
  
  ngOnInit(): void {
    this.loadAttachments();
  }
  
  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['taskId'] || changes['epicId']) && !changes['taskId']?.firstChange && !changes['epicId']?.firstChange) {
      this.loadAttachments();
    }
  }
  
  loadAttachments(): void {
    if (!this.taskId && !this.epicId) {
      return;
    }
    
    this.isLoading = true;
    const loadObservable = this.taskId
      ? this.attachmentService.getTaskAttachments(this.taskId)
      : this.attachmentService.getEpicAttachments(this.epicId!);
    
    loadObservable.subscribe(
      attachments => {
        this.attachments = attachments;
        this.isLoading = false;
      },
      error => {
        console.error('Error loading attachments:', error);
        this.isLoading = false;
      }
    );
  }
  
  downloadAttachment(attachment: AttachmentDTO): void {
    this.attachmentService.downloadFile(attachment.id, attachment.originalFilename);
    this.snackBar.open('Downloading file...', 'Close', { duration: 2000 });
  }
  
  deleteAttachment(attachment: AttachmentDTO): void {
    const dialogRef = this.dialog.open(MatConfirmDialogComponent, {
      width: '400px',
      data: {
        title: 'Delete Attachment',
        message: `Are you sure you want to delete "${attachment.originalFilename}"?`
      }
    });
    
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.attachmentService.deleteAttachment(attachment.id).subscribe(
          () => {
            this.snackBar.open('Attachment deleted successfully', 'Close', { duration: 3000 });
            this.loadAttachments();
          },
          error => {
            const errorMessage = error.error?.message || 'Failed to delete attachment';
            this.snackBar.open(errorMessage, 'Close', { duration: 5000 });
          }
        );
      }
    });
  }
  
  formatFileSize(bytes: number): string {
    return this.attachmentService.formatFileSize(bytes);
  }
  
  getFileIcon(mimeType: string): string {
    return this.attachmentService.getFileIcon(mimeType);
  }
  
  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
