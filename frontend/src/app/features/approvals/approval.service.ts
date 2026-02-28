import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Approval {
  id: string;
  entityType: string;
  entityId: string;
  requestedBy: string;
  requestedAt: string;
  status: string;
  approvedBy?: string;
  approvedAt?: string;
  rejectionReason?: string;
  comments?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ApprovalService {
  private apiUrl = '/api/approvals';

  constructor(private http: HttpClient) {}

  requestApproval(entityId: string, entityType: string, comments?: string): Observable<Approval> {
    let params = new HttpParams()
      .set('entityId', entityId)
      .set('entityType', entityType);
    if (comments) params = params.set('comments', comments);
    return this.http.post<Approval>(this.apiUrl, null, { params });
  }

  getPendingApprovals(): Observable<Approval[]> {
    return this.http.get<Approval[]>(`${this.apiUrl}/pending`);
  }

  approve(approvalId: string, comments?: string): Observable<Approval> {
    const params = comments ? new HttpParams().set('comments', comments) : undefined;
    return this.http.post<Approval>(`${this.apiUrl}/${approvalId}/approve`, null, { params });
  }

  reject(approvalId: string, reason: string): Observable<Approval> {
    const params = new HttpParams().set('reason', reason);
    return this.http.post<Approval>(`${this.apiUrl}/${approvalId}/reject`, null, { params });
  }
}
