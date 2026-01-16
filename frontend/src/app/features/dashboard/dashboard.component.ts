import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { DashboardService, DashboardStats } from '../../core/dashboard.service';
import { CommonModule } from '@angular/common';
import { NgChartsModule } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';

import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [SharedModule, CommonModule, NgChartsModule],
  template: `
    <div class="dashboard-container">
      <div class="dashboard-header">
        <h2>Dashboard</h2>
        <button mat-icon-button (click)="loadDashboardStats()" matTooltip="Refresh">
          <mat-icon>refresh</mat-icon>
        </button>
      </div>
      
      <div *ngIf="loading" class="loading-container">
        <mat-spinner></mat-spinner>
      </div>

      <div *ngIf="!loading && stats" class="dashboard-content">
        <!-- Stats Cards -->
        <div class="stats-grid">
          <mat-card class="dashboard-card primary">
            <mat-card-header>
              <mat-icon>folder</mat-icon>
              <mat-card-title>Total Projects</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <h1>{{ stats.totalProjects }}</h1>
              <p class="subtitle">Active: {{ stats.activeProjects }}</p>
            </mat-card-content>
          </mat-card>

          <mat-card class="dashboard-card info">
            <mat-card-header>
              <mat-icon>list</mat-icon>
              <mat-card-title>Total Tasks</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <h1>{{ stats.totalTasks }}</h1>
              <p class="subtitle">Assigned to me: {{ stats.myAssignedTasks }}</p>
            </mat-card-content>
          </mat-card>

          <mat-card class="dashboard-card warning">
            <mat-card-header>
              <mat-icon>pending</mat-icon>
              <mat-card-title>Pending Tasks</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <h1>{{ stats.pendingTasks }}</h1>
              <p class="subtitle">To Do</p>
            </mat-card-content>
          </mat-card>

          <mat-card class="dashboard-card accent">
            <mat-card-header>
              <mat-icon>hourglass_empty</mat-icon>
              <mat-card-title>In Progress</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <h1>{{ stats.inProgressTasks }}</h1>
              <p class="subtitle">Currently working</p>
            </mat-card-content>
          </mat-card>

          <mat-card class="dashboard-card success">
            <mat-card-header>
              <mat-icon>check_circle</mat-icon>
              <mat-card-title>Completed Tasks</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <h1>{{ stats.completedTasks }}</h1>
              <p class="subtitle">Done</p>
            </mat-card-content>
          </mat-card>

          <mat-card class="dashboard-card danger" *ngIf="stats.overdueTasks > 0">
            <mat-card-header>
              <mat-icon>warning</mat-icon>
              <mat-card-title>Overdue Tasks</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <h1>{{ stats.overdueTasks }}</h1>
              <p class="subtitle">Need attention!</p>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Charts Section -->
        <div class="charts-section" *ngIf="stats.tasksByPriority || stats.tasksByStatus">
          <div class="chart-container" *ngIf="stats.tasksByPriority">
            <mat-card>
              <mat-card-header>
                <mat-card-title>Tasks by Priority</mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <canvas baseChart [data]="priorityChartData" [type]="priorityChartType" [options]="chartOptions"></canvas>
              </mat-card-content>
            </mat-card>
          </div>

          <div class="chart-container" *ngIf="stats.tasksByStatus">
            <mat-card>
              <mat-card-header>
                <mat-card-title>Tasks by Status</mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <canvas baseChart [data]="statusChartData" [type]="statusChartType" [options]="chartOptions"></canvas>
              </mat-card-content>
            </mat-card>
          </div>
        </div>

        <!-- Project Health Section -->
        <div class="project-health-section" *ngIf="stats.projectHealth && stats.projectHealth.length > 0">
          <mat-card>
            <mat-card-header>
              <mat-card-title>
                <mat-icon>health_and_safety</mat-icon>
                Project Health
              </mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div class="health-list">
                <div *ngFor="let project of stats.projectHealth" class="health-item" [ngClass]="'health-' + project.healthStatus.toLowerCase()">
                  <div class="health-info">
                    <strong>{{ project.projectName }}</strong>
                    <span class="health-status">{{ project.healthStatus }}</span>
                  </div>
                  <div class="health-metrics">
                    <div class="progress-bar">
                      <div class="progress-fill" [style.width.%]="project.progressPercentage"></div>
                    </div>
                    <span class="progress-text">{{ project.progressPercentage | number:'1.0-0' }}%</span>
                    <span class="overdue-count" *ngIf="project.overdueTasks > 0">
                      <mat-icon>warning</mat-icon>
                      {{ project.overdueTasks }} overdue
                    </span>
                  </div>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Team Velocity Section -->
        <div class="velocity-section" *ngIf="stats.teamVelocity">
          <mat-card>
            <mat-card-header>
              <mat-card-title>
                <mat-icon>trending_up</mat-icon>
                Team Velocity
              </mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div class="velocity-metrics">
                <div class="velocity-item">
                  <span class="velocity-label">This Week</span>
                  <span class="velocity-value">{{ stats.teamVelocity.tasksCompletedThisWeek }}</span>
                </div>
                <div class="velocity-item">
                  <span class="velocity-label">Last Week</span>
                  <span class="velocity-value">{{ stats.teamVelocity.tasksCompletedLastWeek }}</span>
                </div>
                <div class="velocity-item">
                  <span class="velocity-label">Trend</span>
                  <span class="velocity-value" [ngClass]="stats.teamVelocity.velocityTrend >= 0 ? 'positive' : 'negative'">
                    {{ stats.teamVelocity.velocityTrend >= 0 ? '+' : '' }}{{ stats.teamVelocity.velocityTrend | number:'1.0-0' }}%
                  </span>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Recent Activity Section -->
        <div class="activity-section" *ngIf="stats.recentActivities && stats.recentActivities.length > 0">
          <mat-card>
            <mat-card-header>
              <mat-card-title>
                <mat-icon>history</mat-icon>
                Recent Activity
              </mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div class="activity-list">
                <div *ngFor="let activity of stats.recentActivities" class="activity-item">
                  <mat-icon class="activity-icon">{{ getActivityIcon(activity.action) }}</mat-icon>
                  <div class="activity-content">
                    <div class="activity-text">
                      <strong>{{ activity.username }}</strong>
                      {{ activity.action }}
                      <strong>{{ activity.entityName }}</strong>
                    </div>
                    <span class="activity-time">{{ activity.timestamp | date:'short' }}</span>
                  </div>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>
      </div>
    </div>
  `,
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  stats: DashboardStats | null = null;
  loading = true;

  // Chart configurations
  priorityChartType: ChartType = 'doughnut';
  statusChartType: ChartType = 'pie';
  priorityChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  statusChartData: ChartData<'pie'> = { labels: [], datasets: [] };
  chartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom'
      }
    }
  };

  constructor(private dashboardService: DashboardService) {}

  ngOnInit() {
    this.loadDashboardStats();
  }

  loadDashboardStats() {
    this.loading = true;
    this.dashboardService.getDashboardStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.updateCharts();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading dashboard stats:', err);
        this.loading = false;
      }
    });
  }

  updateCharts() {
    if (this.stats?.tasksByPriority) {
      const labels = Object.keys(this.stats.tasksByPriority);
      const values = Object.values(this.stats.tasksByPriority);
      this.priorityChartData = {
        labels: labels,
        datasets: [{
          data: values,
          backgroundColor: ['#f44336', '#ff9800', '#4caf50']
        }]
      };
    }

    if (this.stats?.tasksByStatus) {
      const labels = Object.keys(this.stats.tasksByStatus);
      const values = Object.values(this.stats.tasksByStatus);
      this.statusChartData = {
        labels: labels,
        datasets: [{
          data: values,
          backgroundColor: ['#3f51b5', '#9c27b0', '#4caf50']
        }]
      };
    }
  }

  getActivityIcon(action: string): string {
    if (action.includes('CREATED')) return 'add_circle';
    if (action.includes('UPDATED')) return 'edit';
    if (action.includes('DELETED')) return 'delete';
    if (action.includes('COMMENT')) return 'comment';
    return 'event';
  }
}
