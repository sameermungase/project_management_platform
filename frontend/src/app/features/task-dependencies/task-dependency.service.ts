import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface TaskDependency {
  id?: string;
  taskId: string;
  taskTitle?: string;
  dependsOnTaskId: string;
  dependsOnTaskTitle?: string;
  dependencyType: 'BLOCKS' | 'BLOCKED_BY' | 'RELATED';
  createdAt?: string;
}

export interface TaskDependencyRequest {
  taskId: string;
  dependsOnTaskId: string;
  dependencyType?: 'BLOCKS' | 'BLOCKED_BY' | 'RELATED';
}

@Injectable({
  providedIn: 'root'
})
export class TaskDependencyService {
  private apiUrl = '/api/task-dependencies';

  constructor(private http: HttpClient) {}

  getDependencies(taskId: string): Observable<TaskDependency[]> {
    return this.http.get<TaskDependency[]>(`${this.apiUrl}/task/${taskId}`);
  }

  getDependentTasks(taskId: string): Observable<TaskDependency[]> {
    return this.http.get<TaskDependency[]>(`${this.apiUrl}/task/${taskId}/dependent`);
  }

  getAllRelatedDependencies(taskId: string): Observable<TaskDependency[]> {
    return this.http.get<TaskDependency[]>(`${this.apiUrl}/task/${taskId}/all`);
  }

  createDependency(dependency: TaskDependencyRequest): Observable<TaskDependency> {
    return this.http.post<TaskDependency>(this.apiUrl, dependency);
  }

  deleteDependency(dependencyId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${dependencyId}`);
  }
}
