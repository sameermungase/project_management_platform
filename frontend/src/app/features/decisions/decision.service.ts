import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DecisionLog {
  id: string;
  projectId: string;
  title: string;
  description: string;
  decisionType?: string;
  decidedBy: string;
  approvedBy?: string;
  status: string;
  visibilityLevel: string;
  createdAt: string;
  updatedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class DecisionService {
  private apiUrl = '/api/projects';

  constructor(private http: HttpClient) {}

  createDecision(projectId: string, title: string, description: string, decisionType?: string, visibilityLevel?: string): Observable<DecisionLog> {
    let params = new HttpParams()
      .set('title', title)
      .set('description', description);
    if (decisionType) params = params.set('decisionType', decisionType);
    if (visibilityLevel) params = params.set('visibilityLevel', visibilityLevel);
    return this.http.post<DecisionLog>(`${this.apiUrl}/${projectId}/decisions`, null, { params });
  }

  getDecisions(projectId: string): Observable<DecisionLog[]> {
    return this.http.get<DecisionLog[]>(`${this.apiUrl}/${projectId}/decisions`);
  }

  getDecision(projectId: string, decisionId: string): Observable<DecisionLog> {
    return this.http.get<DecisionLog>(`${this.apiUrl}/${projectId}/decisions/${decisionId}`);
  }

  approveDecision(projectId: string, decisionId: string): Observable<DecisionLog> {
    return this.http.post<DecisionLog>(`${this.apiUrl}/${projectId}/decisions/${decisionId}/approve`, null);
  }
}
