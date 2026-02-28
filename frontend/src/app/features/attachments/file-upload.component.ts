import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { HttpEventType } from '@angular/common/http';
import { AttachmentService, AttachmentDTO } from './attachment.service';

@Component({
  selector: 'app-file-upload',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressBarModule,
    MatSnackBarModule
  ],
  template: `
    <div class="file-upload-container">
      <div class="upload-zone" 
           (dragover)="onDragOver($event)"
           (dragleave)="onDragLeave($event)"
           (drop)="onDrop($event)"
           [class.drag-active]="isDragging">
        <mat-icon>cloud_upload</mat-icon>
        <p>Drag and drop files here or click to browse</p>
        <p class="small-text">Max file size: 10MB | Allowed: PDF, Images, Office Documents</p>
      </div>
      
      <input type="file"
             #fileInput
             hidden
             (change)="onFileSelected($event)"
             [attr.accept]="acceptedFileTypes">
      
      <div class="button-group">
        <button mat-raised-button color="primary" (click)="fileInput.click()">
          <mat-icon>attach_file</mat-icon>
          Choose File
        </button>
      </div>
      
      <div *ngIf="uploadProgress > 0 && uploadProgress < 100" class="progress-section">
        <mat-progress-bar mode="determinate" [value]="uploadProgress"></mat-progress-bar>
        <p>Uploading... {{ uploadProgress }}%</p>
      </div>
      
      <div *ngIf="selectedFile" class="selected-file-info">
        <mat-icon>{{ getFileIcon(selectedFile.type) }}</mat-icon>
        <div class="file-details">
          <p class="filename">{{ selectedFile.name }}</p>
          <p class="filesize">{{ formatFileSize(selectedFile.size) }}</p>
        </div>
        <button mat-icon-button color="warn" (click)="clearSelection()" [disabled]="isUploading">
          <mat-icon>close</mat-icon>
        </button>
      </div>
      
      <div *ngIf="selectedFile && !isUploading" class="action-buttons">
        <button mat-raised-button color="primary" (click)="uploadFile()">
          <mat-icon>upload</mat-icon>
          Upload File
        </button>
        <button mat-stroked-button (click)="clearSelection()">
          <mat-icon>cancel</mat-icon>
          Cancel
        </button>
      </div>
    </div>
  `,
  styles: [`
    .file-upload-container {
      padding: 20px;
      border-radius: 8px;
      background-color: #f5f5f5;
    }
    
    .upload-zone {
      border: 2px dashed #ccc;
      border-radius: 8px;
      padding: 40px;
      text-align: center;
      cursor: pointer;
      transition: all 0.3s ease;
      background-color: #fafafa;
    }
    
    .upload-zone:hover {
      border-color: #2196f3;
      background-color: #e3f2fd;
    }
    
    .upload-zone.drag-active {
      border-color: #2196f3;
      background-color: #e3f2fd;
      box-shadow: 0 0 10px rgba(33, 150, 243, 0.3);
    }
    
    .upload-zone mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: #2196f3;
      margin-bottom: 10px;
    }
    
    .upload-zone p {
      margin: 8px 0;
      color: #333;
    }
    
    .small-text {
      font-size: 12px;
      color: #999;
      margin-top: 10px;
    }
    
    .button-group {
      margin-top: 20px;
      display: flex;
      gap: 10px;
    }
    
    .progress-section {
      margin-top: 20px;
    }
    
    .selected-file-info {
      display: flex;
      align-items: center;
      gap: 15px;
      padding: 15px;
      margin-top: 20px;
      background-color: white;
      border-radius: 8px;
      border-left: 4px solid #2196f3;
    }
    
    .selected-file-info mat-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      color: #2196f3;
    }
    
    .file-details {
      flex: 1;
    }
    
    .filename {
      margin: 0;
      font-weight: 500;
      word-break: break-word;
    }
    
    .filesize {
      margin: 5px 0 0 0;
      font-size: 12px;
      color: #999;
    }
    
    .action-buttons {
      display: flex;
      gap: 10px;
      margin-top: 15px;
    }
  `]
})
export class FileUploadComponent implements OnInit {
  @Input() taskId?: string;
  @Input() epicId?: string;
  @Output() uploadSuccess = new EventEmitter<AttachmentDTO>();
  @Output() uploadError = new EventEmitter<string>();
  
  selectedFile: File | null = null;
  isDragging = false;
  isUploading = false;
  uploadProgress = 0;
  acceptedFileTypes = '.pdf,.jpg,.jpeg,.png,.gif,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt';
  
  constructor(
    private attachmentService: AttachmentService,
    private snackBar: MatSnackBar
  ) {}
  
  ngOnInit(): void {
    if (!this.taskId && !this.epicId) {
      console.error('Either taskId or epicId must be provided');
    }
  }
  
  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = true;
  }
  
  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
  }
  
  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging = false;
    
    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.selectedFile = files[0];
    }
  }
  
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
    }
  }
  
  uploadFile(): void {
    if (!this.selectedFile) {
      this.snackBar.open('Please select a file first', 'Close', { duration: 3000 });
      return;
    }
    
    if (!this.taskId && !this.epicId) {
      this.snackBar.open('Task or Epic ID is required', 'Close', { duration: 3000 });
      return;
    }
    
    this.isUploading = true;
    this.uploadProgress = 0;
    
    const uploadObservable = this.taskId
      ? this.attachmentService.uploadToTask(this.taskId, this.selectedFile)
      : this.attachmentService.uploadToEpic(this.epicId!, this.selectedFile);
    
    uploadObservable.subscribe(
      event => {
        if (event.type === HttpEventType.UploadProgress) {
          if (event.total) {
            this.uploadProgress = Math.round((100 * event.loaded) / event.total);
          }
        } else if (event.type === HttpEventType.Response) {
          this.uploadProgress = 100;
          this.isUploading = false;
          const attachment = event.body as AttachmentDTO;
          this.snackBar.open('File uploaded successfully!', 'Close', { duration: 3000 });
          this.uploadSuccess.emit(attachment);
          this.clearSelection();
        }
      },
      error => {
        this.isUploading = false;
        this.uploadProgress = 0;
        const errorMessage = error.error?.message || 'Failed to upload file';
        this.snackBar.open(errorMessage, 'Close', { duration: 5000 });
        this.uploadError.emit(errorMessage);
      }
    );
  }
  
  clearSelection(): void {
    this.selectedFile = null;
    this.uploadProgress = 0;
  }
  
  formatFileSize(bytes: number): string {
    return this.attachmentService.formatFileSize(bytes);
  }
  
  getFileIcon(mimeType: string): string {
    return this.attachmentService.getFileIcon(mimeType);
  }
}
