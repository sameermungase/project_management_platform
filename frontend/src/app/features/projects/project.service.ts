import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Project {
  id?: string;
  name: string;
  description: string;
  ownerId?: string;
  ownerUsername?: string;
  memberIds?: string[];
  createdAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private apiUrl = '/api/projects';

  constructor(private http: HttpClient) {}

  getProjects(page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(this.apiUrl, { params });
  }

  getProject(id: string): Observable<Project> {
    return this.http.get<Project>(`${this.apiUrl}/${id}`);
  }

  createProject(project: Project): Observable<Project> {
    return this.http.post<Project>(this.apiUrl, project);
  }

  updateProject(id: string, project: Project): Observable<Project> {
    return this.http.put<Project>(`${this.apiUrl}/${id}`, project);
  }

  deleteProject(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  addMemberToProject(projectId: string, userId: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${projectId}/members`, { userId });
  }

  removeMemberFromProject(projectId: string, userId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${projectId}/members/${userId}`);
  }

  getAllProjectsAdmin(page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/admin/all`, { params });
  }
}
