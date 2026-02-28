import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ProjectService, Project } from './project.service';
import { UserService, User } from '../users/user.service';
import { HttpClient } from '@angular/common/http';

export interface ProjectMemberRole {
  id?: string;
  projectId: string;
  userId: string;
  role: string;
  assignedBy?: string;
  createdAt?: string;
}

@Component({
  selector: 'app-project-role-assignment-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatListModule,
    MatSelectModule,
    MatFormFieldModule,
    MatIconModule,
    MatSnackBarModule,
    MatCardModule,
    MatTabsModule,
    MatChipsModule,
    MatTooltipModule
  ],
  template: `
    <h2 mat-dialog-title>Project Role Assignment: {{ data.project.name }}</h2>
    <mat-dialog-content>
      <mat-tab-group>
        <!-- Current Roles Tab -->
        <mat-tab label="Current Roles">
          <div class="tab-content">
            <h3 *ngIf="memberRoles.length === 0">No role assignments yet</h3>
            <mat-list *ngIf="memberRoles.length > 0">
              <mat-list-item *ngFor="let memberRole of memberRoles" class="role-item">
                <div class="member-info">
                  <strong matListItemTitle>{{ getMemberName(memberRole.userId) }}</strong>
                  <span matListItemLine>{{ memberRole.role }}</span>
                </div>
                <mat-chip-set>
                  <mat-chip [color]="getRoleColor(memberRole.role)" selected>
                    {{ memberRole.role }}
                  </mat-chip>
                </mat-chip-set>
                <button 
                  mat-icon-button 
                  color="warn" 
                  (click)="removeRole(memberRole)" 
                  [disabled]="loading"
                  matTooltip="Remove role">
                  <mat-icon>remove_circle</mat-icon>
                </button>
              </mat-list-item>
            </mat-list>
          </div>
        </mat-tab>

        <!-- Assign Role Tab -->
        <mat-tab label="Assign Role">
          <div class="tab-content">
            <form [formGroup]="roleForm" (ngSubmit)="assignRole()">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Select Member</mat-label>
                <mat-select formControlName="userId">
                  <mat-option *ngFor="let member of projectMembers" [value]="member.id">
                    {{ member.username }} ({{ member.email }})
                  </mat-option>
                </mat-select>
                <mat-error *ngIf="roleForm.get('userId')?.hasError('required')">
                  Please select a member
                </mat-error>
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Select Role</mat-label>
                <mat-select formControlName="role">
                  <mat-option value="USER">User</mat-option>
                  <mat-option value="MANAGER">Manager</mat-option>
                  <mat-option value="STAFF">Staff</mat-option>
                  <mat-option value="SENIOR">Senior</mat-option>
                  <mat-option value="PRINCIPAL">Principal</mat-option>
                  <mat-option value="ARCHITECT">Architect</mat-option>
                </mat-select>
                <mat-error *ngIf="roleForm.get('role')?.hasError('required')">
                  Please select a role
                </mat-error>
              </mat-form-field>

              <div class="role-description" *ngIf="roleForm.get('role')?.value">
                <strong>Role Description:</strong>
                <p>{{ getRoleDescription(roleForm.get('role')?.value) }}</p>
              </div>

              <button 
                mat-raised-button 
                color="primary" 
                type="submit"
                [disabled]="roleForm.invalid || loading"
                class="full-width">
                {{ loading ? 'Assigning...' : 'Assign Role' }}
              </button>
            </form>
          </div>
        </mat-tab>
      </mat-tab-group>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Close</button>
    </mat-dialog-actions>
  `,
  styles: [`
    mat-tab-group {
      width: 100%;
      margin-top: 20px;
    }

    .tab-content {
      padding: 20px;
    }

    .full-width {
      width: 100%;
      margin-bottom: 15px;
    }

    .role-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px 0;
      border-bottom: 1px solid #eee;
    }

    .member-info {
      flex: 1;
    }

    .role-description {
      background-color: #f5f5f5;
      padding: 12px;
      border-radius: 4px;
      margin-bottom: 15px;
      border-left: 3px solid #3f51b5;
    }

    .role-description strong {
      display: block;
      margin-bottom: 8px;
      color: #333;
    }

    .role-description p {
      margin: 0;
      color: #666;
      font-size: 12px;
      line-height: 1.4;
    }

    h3 {
      text-align: center;
      color: #999;
      margin: 40px 0;
    }
  `]
})
export class ProjectRoleAssignmentDialogComponent implements OnInit {
  roleForm: FormGroup;
  projectMembers: User[] = [];
  memberRoles: ProjectMemberRole[] = [];
  loading = false;

  constructor(
    private fb: FormBuilder,
    private projectService: ProjectService,
    private userService: UserService,
    private http: HttpClient,
    public dialogRef: MatDialogRef<ProjectRoleAssignmentDialogComponent>,
    private snackBar: MatSnackBar,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.roleForm = this.fb.group({
      userId: ['', Validators.required],
      role: ['USER', Validators.required]
    });
  }

  ngOnInit() {
    this.loadProjectMembers();
    this.loadMemberRoles();
  }

  loadProjectMembers() {
    if (!this.data.project.id) return;

    this.userService.getUsers().subscribe({
      next: (response) => {
        // Filter to only show project members
        this.projectMembers = (response as any).content || response;
        // Remove members that already have roles
        this.updateAvailableMembers();
      },
      error: (error) => {
        console.error('Failed to load users:', error);
      }
    });
  }

  loadMemberRoles() {
    if (!this.data.project.id) return;

    // Call the backend endpoint to get all member roles for this project
    this.http.get<ProjectMemberRole[]>(
      `/api/projects/${this.data.project.id}/members/all/roles`
    ).subscribe({
      next: (data) => {
        this.memberRoles = data;
        this.updateAvailableMembers();
      },
      error: (error) => {
        console.error('Failed to load member roles:', error);
      }
    });
  }

  updateAvailableMembers() {
    // Show members who don't have roles assigned
    this.projectMembers = this.projectMembers.filter(
      member => !this.memberRoles.some(role => role.userId === member.id)
    );
  }

  assignRole() {
    if (this.roleForm.valid && this.data.project.id) {
      this.loading = true;
      const formValue = this.roleForm.value;

      // Call the backend endpoint to assign role
      this.http.post<ProjectMemberRole>(
        `/api/projects/${this.data.project.id}/members/${formValue.userId}/role?role=${formValue.role}`,
        null
      ).subscribe({
        next: (result) => {
          this.snackBar.open('Role assigned successfully', 'Close', { duration: 3000 });
          this.roleForm.reset({ role: 'USER' });
          this.loading = false;
          this.loadMemberRoles();
        },
        error: (error) => {
          this.loading = false;
          this.snackBar.open('Failed to assign role', 'Close', { duration: 3000 });
          console.error(error);
        }
      });
    }
  }

  removeRole(memberRole: ProjectMemberRole) {
    if (confirm('Are you sure you want to remove this role assignment?')) {
      this.loading = true;

      this.http.delete<void>(
        `/api/projects/${this.data.project.id}/members/${memberRole.userId}/role`
      ).subscribe({
        next: () => {
          this.snackBar.open('Role removed successfully', 'Close', { duration: 3000 });
          this.loading = false;
          this.loadMemberRoles();
        },
        error: (error) => {
          this.loading = false;
          this.snackBar.open('Failed to remove role', 'Close', { duration: 3000 });
          console.error(error);
        }
      });
    }
  }

  getMemberName(userId: string): string {
    // First try to find in project members
    const member = this.projectMembers.find(m => m.id === userId);
    if (member) return member.username;
    
    // If not found, it might be already assigned, so just show abbreviated ID
    return userId.substring(0, 8) + '...';
  }

  getRoleColor(role: string): 'primary' | 'accent' | 'warn' {
    switch (role) {
      case 'USER':
        return 'primary';
      case 'MANAGER':
        return 'accent';
      case 'STAFF':
      case 'SENIOR':
      case 'PRINCIPAL':
      case 'ARCHITECT':
        return 'warn';
      default:
        return 'primary';
    }
  }

  getRoleDescription(role: string): string {
    const descriptions: { [key: string]: string } = {
      USER: 'Basic user with limited permissions. Can view and comment on tasks.',
      MANAGER: 'Can manage project members and view reports.',
      STAFF: 'Can create and manage tasks within the project.',
      SENIOR: 'Can assign roles and manage project configuration.',
      PRINCIPAL: 'High-level access with strategic decision-making capability.',
      ARCHITECT: 'Full system access for architectural decisions.'
    };
    return descriptions[role] || 'User role';
  }
}
