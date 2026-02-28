import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Epic {
  id: string;
  projectId: string;
  title: string;
  description?: string;
  status: string;
  visibilityLevel: string;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class EpicService {
  private apiUrl = '/api/projects';
  private approvalsUrl = '/api/approvals';

  constructor(private http: HttpClient) {}

  createEpic(projectId: string, title: string, description?: string, visibilityLevel?: string): Observable<Epic> {
    const params = new HttpParams()
      .set('title', title)
      .set('description', description || '')
      .set('visibilityLevel', visibilityLevel || 'TEAM');
    return this.http.post<Epic>(`${this.apiUrl}/${projectId}/epics`, null, { params });
  }

  getEpics(projectId: string): Observable<Epic[]> {
    return this.http.get<Epic[]>(`${this.apiUrl}/${projectId}/epics`);
  }

  getEpicsByProject(projectId: string): Observable<Epic[]> {
    return this.getEpics(projectId);
  }

  getEpic(projectId: string, epicId: string): Observable<Epic> {
    return this.http.get<Epic>(`${this.apiUrl}/${projectId}/epics/${epicId}`);
  }

  updateEpic(projectId: string, epicId: string, title?: string, description?: string, status?: string): Observable<Epic> {
    let params = new HttpParams();
    if (title) params = params.set('title', title);
    if (description) params = params.set('description', description);
    if (status) params = params.set('status', status);
    return this.http.put<Epic>(`${this.apiUrl}/${projectId}/epics/${epicId}`, null, { params });
  }

  deleteEpic(projectId: string, epicId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${projectId}/epics/${epicId}`);
  }

  requestApproval(epicId: string, comments?: string): Observable<any> {
    let params = new HttpParams()
      .set('entityId', epicId)
      .set('entityType', 'EPIC');
    if (comments) params = params.set('comments', comments);
    return this.http.post<any>(`${this.approvalsUrl}`, null, { params });
  }
}
