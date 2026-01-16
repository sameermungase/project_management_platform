import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DashboardStats {
  totalProjects: number;
  activeProjects: number;
  totalTasks: number;
  pendingTasks: number;
  completedTasks: number;
  inProgressTasks: number;
  overdueTasks: number;
  myAssignedTasks: number;
  tasksByPriority?: { [key: string]: number };
  tasksByStatus?: { [key: string]: number };
  recentActivities?: RecentActivity[];
  projectHealth?: ProjectHealth[];
  teamVelocity?: TeamVelocity;
  taskDistribution?: TaskDistribution;
}

export interface RecentActivity {
  action: string;
  entityType: string;
  entityName: string;
  username: string;
  timestamp: string;
}

export interface ProjectHealth {
  projectId: string;
  projectName: string;
  healthStatus: 'ON_TRACK' | 'AT_RISK' | 'DELAYED';
  progressPercentage: number;
  overdueTasks: number;
}

export interface TeamVelocity {
  averageVelocity: number;
  tasksCompletedThisWeek: number;
  tasksCompletedLastWeek: number;
  velocityTrend: number;
}

export interface TaskDistribution {
  tasksByAssignee: { [key: string]: number };
  tasksByProject: { [key: string]: number };
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = 'http://localhost:8080/api/dashboard';

  constructor(private http: HttpClient) {}

  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/stats`);
  }

  getAdminDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.apiUrl}/admin/stats`);
  }
}
