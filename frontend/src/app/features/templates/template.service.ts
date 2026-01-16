import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TemplateTask {
  id?: string;
  title: string;
  description?: string;
  priority?: string;
  orderIndex?: number;
}

export interface ProjectTemplate {
  id?: string;
  name: string;
  description?: string;
  templateType?: string;
  createdById?: string;
  createdByUsername?: string;
  isPublic?: boolean;
  templateTasks?: TemplateTask[];
  createdAt?: string;
  updatedAt?: string;
}

export interface ProjectTemplateRequest {
  name: string;
  description?: string;
  templateType?: string;
  isPublic?: boolean;
  templateTasks?: TemplateTaskRequest[];
}

export interface TemplateTaskRequest {
  title: string;
  description?: string;
  priority?: string;
  orderIndex?: number;
}

@Injectable({
  providedIn: 'root'
})
export class TemplateService {
  private apiUrl = 'http://localhost:8080/api/templates';

  constructor(private http: HttpClient) {}

  getAllTemplates(): Observable<ProjectTemplate[]> {
    return this.http.get<ProjectTemplate[]>(this.apiUrl);
  }

  getPublicTemplates(): Observable<ProjectTemplate[]> {
    return this.http.get<ProjectTemplate[]>(`${this.apiUrl}/public`);
  }

  getTemplateById(templateId: string): Observable<ProjectTemplate> {
    return this.http.get<ProjectTemplate>(`${this.apiUrl}/${templateId}`);
  }

  createTemplate(template: ProjectTemplateRequest): Observable<ProjectTemplate> {
    return this.http.post<ProjectTemplate>(this.apiUrl, template);
  }

  deleteTemplate(templateId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${templateId}`);
  }

  createProjectFromTemplate(templateId: string, project: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${templateId}/create-project`, project);
  }
}
