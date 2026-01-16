import { Component, OnInit } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { UserService, OrganizationUserDTO } from './user.service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { RoleEditDialogComponent } from './role-edit-dialog.component';

@Component({
  selector: 'app-organization',
  standalone: true,
  imports: [SharedModule],
  template: `
    <div class="organization-container">
      <div class="header">
        <h2>Organization Management</h2>
        <button mat-raised-button color="primary" (click)="refreshData()">
          <mat-icon>refresh</mat-icon> Refresh
        </button>
      </div>

      <div *ngIf="loading" class="loading-container">
        <mat-spinner></mat-spinner>
      </div>

      <div *ngIf="!loading" class="users-grid">
        <mat-card *ngFor="let user of users" class="user-card">
          <mat-card-header>
            <div mat-card-avatar class="user-avatar">
              <mat-icon>person</mat-icon>
            </div>
            <mat-card-title>{{ user.username }}</mat-card-title>
            <mat-card-subtitle>{{ user.email }}</mat-card-subtitle>
          </mat-card-header>
          
          <mat-card-content>
            <div class="roles-section">
              <strong>Roles:</strong>
              <mat-chip-listbox>
                <mat-chip *ngFor="let role of user.roles" [highlighted]="true">
                  {{ role }}
                </mat-chip>
              </mat-chip-listbox>
            </div>

            <div class="stats-section">
              <div class="stat">
                <mat-icon>folder</mat-icon>
                <span>{{ user.projectCount }} Projects</span>
              </div>
              <div class="stat">
                <mat-icon>assignment</mat-icon>
                <span>{{ user.taskCount }} Tasks</span>
              </div>
            </div>

            <mat-expansion-panel *ngIf="user.activeProjects && user.activeProjects.length > 0">
              <mat-expansion-panel-header>
                <mat-panel-title>Projects ({{ user.activeProjects.length }})</mat-panel-title>
              </mat-expansion-panel-header>
              <mat-list dense>
                <mat-list-item *ngFor="let project of user.activeProjects">
                  <mat-icon matListItemIcon>folder</mat-icon>
                  <div matListItemTitle>{{ project.name }}</div>
                  <div matListItemLine>{{ project.role }}</div>
                </mat-list-item>
              </mat-list>
            </mat-expansion-panel>

            <mat-expansion-panel *ngIf="user.activeTasks && user.activeTasks.length > 0">
              <mat-expansion-panel-header>
                <mat-panel-title>Tasks ({{ user.activeTasks.length }})</mat-panel-title>
              </mat-expansion-panel-header>
              <mat-list dense>
                <mat-list-item *ngFor="let task of user.activeTasks">
                  <mat-icon matListItemIcon [style.color]="getStatusColor(task.status)">
                    {{ getStatusIcon(task.status) }}
                  </mat-icon>
                  <div matListItemTitle>{{ task.title }}</div>
                  <div matListItemLine>{{ task.projectName }} - {{ task.status }}</div>
                </mat-list-item>
              </mat-list>
            </mat-expansion-panel>
          </mat-card-content>

          <mat-card-actions>
            <button mat-button color="primary" (click)="editUserRoles(user)">
              <mat-icon>edit</mat-icon> Edit Roles
            </button>
            <button mat-button color="warn" (click)="deleteUser(user)">
              <mat-icon>delete</mat-icon> Remove
            </button>
          </mat-card-actions>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .organization-container { padding: 20px; }
    .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    .header h2 { margin: 0; color: #333; }
    .loading-container { display: flex; justify-content: center; padding: 50px; }
    .users-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(400px, 1fr)); gap: 20px; }
    .user-card { transition: transform 0.2s; }
    .user-card:hover { transform: translateY(-5px); box-shadow: 0 4px 8px rgba(0,0,0,0.2); }
    .user-avatar { 
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      width: 50px;
      height: 50px;
      border-radius: 50%;
    }
    .user-avatar mat-icon { color: white; }
    .roles-section { margin: 15px 0; }
    .roles-section strong { display: block; margin-bottom: 8px; }
    .stats-section { display: flex; gap: 20px; margin: 15px 0; }
    .stat { display: flex; align-items: center; gap: 5px; color: #666; }
    .stat mat-icon { font-size: 18px; width: 18px; height: 18px; }
    mat-expansion-panel { margin-top: 10px; }
    mat-card-actions { justify-content: flex-end; }
  `]
})
export class OrganizationComponent implements OnInit {
  users: OrganizationUserDTO[] = [];
  loading = true;

  constructor(
    private userService: UserService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.loadOrganizationUsers();
  }

  loadOrganizationUsers() {
    this.loading = true;
    this.userService.getOrganizationUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading organization users:', err);
        this.snackBar.open('Error loading users', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  refreshData() {
    this.loadOrganizationUsers();
  }

  editUserRoles(user: OrganizationUserDTO) {
    // TODO: Open dialog to edit roles
    this.snackBar.open('Edit roles feature - coming soon', 'Close', { duration: 2000 });
  }

  deleteUser(user: OrganizationUserDTO) {
    if (confirm(`Are you sure you want to remove ${user.username}?`)) {
      this.userService.deleteUser(user.id).subscribe({
        next: () => {
          this.snackBar.open('User removed successfully', 'Close', { duration: 3000 });
          this.loadOrganizationUsers();
        },
        error: (err) => {
          console.error('Error deleting user:', err);
          this.snackBar.open('Error removing user', 'Close', { duration: 3000 });
        }
      });
    }
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
}
