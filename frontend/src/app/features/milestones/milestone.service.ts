import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Milestone {
  id: string;
  projectId: string;
  epicId?: string;
  title: string;
  description?: string;
  targetDate?: string;
  status: string;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class MilestoneService {
  private apiUrl = '/api/projects';
  private approvalsUrl = '/api/approvals';

  constructor(private http: HttpClient) {}

  createMilestone(projectId: string, title: string, epicId?: string, description?: string, targetDate?: string): Observable<Milestone> {
    let params = new HttpParams().set('title', title);
    if (epicId) params = params.set('epicId', epicId);
    if (description) params = params.set('description', description);
    if (targetDate) params = params.set('targetDate', targetDate);
    return this.http.post<Milestone>(`${this.apiUrl}/${projectId}/milestones`, null, { params });
  }

  getMilestones(projectId: string): Observable<Milestone[]> {
    return this.http.get<Milestone[]>(`${this.apiUrl}/${projectId}/milestones`);
  }

  getMilestonesByProject(projectId: string): Observable<Milestone[]> {
    return this.getMilestones(projectId);
  }

  getMilestone(projectId: string, milestoneId: string): Observable<Milestone> {
    return this.http.get<Milestone>(`${this.apiUrl}/${projectId}/milestones/${milestoneId}`);
  }

  updateMilestone(projectId: string, milestoneId: string, title?: string, description?: string, targetDate?: string, status?: string): Observable<Milestone> {
    let params = new HttpParams();
    if (title) params = params.set('title', title);
    if (description) params = params.set('description', description);
    if (targetDate) params = params.set('targetDate', targetDate);
    if (status) params = params.set('status', status);
    return this.http.put<Milestone>(`${this.apiUrl}/${projectId}/milestones/${milestoneId}`, null, { params });
  }

  deleteMilestone(projectId: string, milestoneId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${projectId}/milestones/${milestoneId}`);
  }

  requestApproval(milestoneId: string, comments?: string): Observable<any> {
    let params = new HttpParams()
      .set('entityId', milestoneId)
      .set('entityType', 'MILESTONE');
    if (comments) params = params.set('comments', comments);
    return this.http.post<any>(`${this.approvalsUrl}`, null, { params });
  }
}
