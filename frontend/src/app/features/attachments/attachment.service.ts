import { Injectable } from '@angular/core';
import { HttpClient, HttpEvent, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AttachmentDTO {
  id: string;
  filename: string;
  originalFilename: string;
  mimeType: string;
  fileSize: number;
  createdAt: string;
  updatedAt: string;
  createdById: string;
  createdByName: string;
  taskId?: string;
  epicId?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AttachmentService {
  private apiUrl = '/api/attachments';

  constructor(private http: HttpClient) {}

  /**
   * Upload file to task
   */
  uploadToTask(taskId: string, file: File): Observable<HttpEvent<AttachmentDTO>> {
    const formData = new FormData();
    formData.append('file', file);

    const req = new HttpRequest('POST', `${this.apiUrl}/task/${taskId}/upload`, formData, {
      reportProgress: true,
      responseType: 'json'
    });

    return this.http.request<AttachmentDTO>(req);
  }

  /**
   * Upload file to epic
   */
  uploadToEpic(epicId: string, file: File): Observable<HttpEvent<AttachmentDTO>> {
    const formData = new FormData();
    formData.append('file', file);

    const req = new HttpRequest('POST', `${this.apiUrl}/epic/${epicId}/upload`, formData, {
      reportProgress: true,
      responseType: 'json'
    });

    return this.http.request<AttachmentDTO>(req);
  }

  /**
   * Download file
   */
  downloadFile(attachmentId: string, originalFilename: string): void {
    this.http.get(`${this.apiUrl}/${attachmentId}/download`, { responseType: 'blob' })
      .subscribe(
        (blob: Blob) => {
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');
          link.href = url;
          link.download = originalFilename;
          link.click();
          window.URL.revokeObjectURL(url);
        },
        error => {
          console.error('Error downloading file:', error);
        }
      );
  }

  /**
   * Delete attachment
   */
  deleteAttachment(attachmentId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${attachmentId}`);
  }

  /**
   * Get task attachments
   */
  getTaskAttachments(taskId: string): Observable<AttachmentDTO[]> {
    return this.http.get<AttachmentDTO[]>(`${this.apiUrl}/task/${taskId}`);
  }

  /**
   * Get epic attachments
   */
  getEpicAttachments(epicId: string): Observable<AttachmentDTO[]> {
    return this.http.get<AttachmentDTO[]>(`${this.apiUrl}/epic/${epicId}`);
  }

  /**
   * Format file size for display
   */
  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  }

  /**
   * Get file icon based on MIME type
   */
  getFileIcon(mimeType: string): string {
    if (mimeType.startsWith('image/')) return 'image';
    if (mimeType === 'application/pdf') return 'picture_as_pdf';
    if (mimeType.includes('spreadsheet') || mimeType.includes('sheet')) return 'table_chart';
    if (mimeType.includes('presentation')) return 'slideshow';
    if (mimeType.includes('wordprocessingml') || mimeType.includes('document')) return 'description';
    return 'attachment';
  }
}
