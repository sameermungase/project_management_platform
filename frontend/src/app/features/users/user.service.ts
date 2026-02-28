import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface User {
  id: string;
  username: string;
  email: string;
  roles: string[];
}

export interface ProjectSummaryDTO {
  id: string;
  name: string;
  ownerUsername: string;
  role: string;
}

export interface TaskSummaryDTO {
  id: string;
  title: string;
  status: string;
  priority: string;
  dueDate: string;
  projectName: string;
}

export interface UserDetailDTO {
  id: string;
  username: string;
  email: string;
  roles: string[];
  createdAt: string;
  updatedAt: string;
  projects: ProjectSummaryDTO[];
  assignedTasks: TaskSummaryDTO[];
}

export interface OrganizationUserDTO {
  id: string;
  username: string;
  email: string;
  roles: string[];
  createdAt: string;
  projectCount: number;
  taskCount: number;
  activeProjects: ProjectSummaryDTO[];
  activeTasks: TaskSummaryDTO[];
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = '/api/users';

  constructor(private http: HttpClient) { }

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }

  getCurrentUserProfile(): Observable<UserDetailDTO> {
    return this.http.get<UserDetailDTO>(`${this.apiUrl}/profile`);
  }

  getUserProfile(id: string): Observable<UserDetailDTO> {
    return this.http.get<UserDetailDTO>(`${this.apiUrl}/profile/${id}`);
  }

  getOrganizationUsers(): Observable<OrganizationUserDTO[]> {
    return this.http.get<OrganizationUserDTO[]>(`${this.apiUrl}/organization`);
  }

  updateUserRoles(id: string, roles: string[]): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}/roles`, { roles });
  }

  deleteUser(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
