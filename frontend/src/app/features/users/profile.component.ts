import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { UserService, UserDetailDTO } from '../users/user.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [SharedModule],
  template: `
    <div class="profile-container">
      <h2>My Profile</h2>

      <div *ngIf="loading" class="loading-container">
        <mat-spinner></mat-spinner>
      </div>

      <div *ngIf="!loading && profile" class="profile-content">
        <!-- User Info Card -->
        <mat-card class="profile-card">
          <mat-card-header>
            <div mat-card-avatar class="profile-avatar">
              <mat-icon>person</mat-icon>
            </div>
            <mat-card-title>{{ profile.username }}</mat-card-title>
            <mat-card-subtitle>{{ profile.email }}</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content>
            <div class="info-section">
              <h3>Roles & Permissions</h3>
              <mat-chip-listbox>
                <mat-chip *ngFor="let role of profile.roles" [highlighted]="true">
                  {{ role }}
                </mat-chip>
              </mat-chip-listbox>
            </div>

            <div class="info-section">
              <p><strong>Member Since:</strong> {{ profile.createdAt | date: 'longDate' }}</p>
              <p><strong>Last Updated:</strong> {{ profile.updatedAt | date: 'short' }}</p>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Projects Section -->
        <mat-card class="section-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>folder</mat-icon>
              My Projects ({{ profile.projects.length || 0 }})
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div *ngIf="profile.projects && profile.projects.length > 0" class="projects-list">
              <mat-list>
                <mat-list-item *ngFor="let project of profile.projects">
                  <mat-icon matListItemIcon>{{ project.role === 'OWNER' ? 'stars' : 'people' }}</mat-icon>
                  <div matListItemTitle>{{ project.name }}</div>
                  <div matListItemLine>{{ project.role }} • Owner: {{ project.ownerUsername }}</div>
                </mat-list-item>
              </mat-list>
            </div>
            <p *ngIf="!profile.projects || profile.projects.length === 0" class="empty-message">
              No projects assigned yet.
            </p>
          </mat-card-content>
        </mat-card>

        <!-- Tasks Section -->
        <mat-card class="section-card">
          <mat-card-header>
            <mat-card-title>
              <mat-icon>assignment</mat-icon>
              My Tasks ({{ profile.assignedTasks.length || 0 }})
            </mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div *ngIf="profile.assignedTasks && profile.assignedTasks.length > 0" class="tasks-list">
              <mat-list>
                <mat-list-item *ngFor="let task of profile.assignedTasks">
                  <mat-icon matListItemIcon [style.color]="getStatusColor(task.status)">
                    {{ getStatusIcon(task.status) }}
                  </mat-icon>
                  <div matListItemTitle>{{ task.title }}</div>
                  <div matListItemLine>
                    {{ task.projectName }} • {{ task.status }} 
                    <span *ngIf="task.dueDate"> • Due: {{ task.dueDate | date: 'shortDate' }}</span>
                  </div>
                  <mat-chip [highlighted]="true" [style.background-color]="getPriorityColor(task.priority)">
                    {{ task.priority }}
                  </mat-chip>
                </mat-list-item>
              </mat-list>
            </div>
            <p *ngIf="!profile.assignedTasks || profile.assignedTasks.length === 0" class="empty-message">
              No tasks assigned yet.
            </p>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .profile-container { padding: 20px; max-width: 1200px; margin: 0 auto; }
    h2 { margin-bottom: 20px; color: #333; }
    .loading-container { display: flex; justify-content: center; padding: 50px; }
    .profile-content { display: grid; gap: 20px; }
    .profile-card { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }
    .profile-card mat-card-header { color: white; }
    .profile-card mat-card-subtitle { color: rgba(255,255,255,0.8); }
    .profile-avatar { 
      background: rgba(255,255,255,0.3); 
      display: flex; 
      align-items: center; 
      justify-content: center;
      width: 60px;
      height: 60px;
      border-radius: 50%;
    }
    .profile-avatar mat-icon { font-size: 40px; width: 40px; height: 40px; color: white; }
    .info-section { margin: 20px 0; }
    .info-section h3 { margin-bottom: 10px; font-size: 1.1em; }
    .info-section p { margin: 8px 0; }
    .section-card mat-card-title { display: flex; align-items: center; gap: 10px; }
    .projects-list, .tasks-list { max-height: 400px; overflow-y: auto; }
    .empty-message { text-align: center; padding: 20px; color: #999; }
    mat-chip { margin: 4px; }
  `]
})
export class ProfileComponent implements OnInit {
  profile: UserDetailDTO | null = null;
  loading = true;

  constructor(private userService: UserService) {}

  ngOnInit() {
    this.loadProfile();
  }

  loadProfile() {
    this.loading = true;
    this.userService.getCurrentUserProfile().subscribe({
      next: (data) => {
        this.profile = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading profile:', err);
        this.loading = false;
      }
    });
  }

  getStatusColor(status: string): string {
    const colors: any = {
      'TO_DO': '#ff9800',
      'IN_PROGRESS': '#2196f3',
      'DONE': '#4caf50',
      'BLOCKED': '#f44336'
    };
    return colors[status] || '#999';
  }

  getStatusIcon(status: string): string {
    const icons: any = {
      'TO_DO': 'radio_button_unchecked',
      'IN_PROGRESS': 'hourglass_empty',
      'DONE': 'check_circle',
      'BLOCKED': 'block'
    };
    return icons[status] || 'circle';
  }

  getPriorityColor(priority: string): string {
    const colors: any = {
      'LOW': '#4caf50',
      'MEDIUM': '#ff9800',
      'HIGH': '#f44336',
      'CRITICAL': '#9c27b0'
    };
    return colors[priority] || '#999';
  }
}
