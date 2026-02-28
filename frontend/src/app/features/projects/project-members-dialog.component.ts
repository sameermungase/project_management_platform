import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Project, ProjectService } from './project.service';
import { UserService, User } from '../users/user.service';
import { ProjectRoleAssignmentDialogComponent } from './project-role-assignment-dialog.component';

@Component({
  selector: 'app-project-members-dialog',
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
    MatSnackBarModule
  ],
  template: `
    <h2 mat-dialog-title>Manage Members: {{ data.project.name }}</h2>
    <mat-dialog-content>
      <div class="members-container">
        <div class="header-actions">
          <h3>Current Members</h3>
          <button mat-raised-button color="accent" (click)="openRoleAssignmentDialog()" matTooltip="Manage project-level roles">
            <mat-icon>security</mat-icon> Manage Roles
          </button>
        </div>
        <mat-list>
          <mat-list-item *ngFor="let member of currentMembers">
            <span matListItemTitle>{{ member.username }}</span>
            <span matListItemLine>{{ member.email }}</span>
            <button mat-icon-button color="warn" (click)="removeMember(member)" [disabled]="loading">
              <mat-icon>remove_circle</mat-icon>
            </button>
          </mat-list-item>
          <mat-list-item *ngIf="currentMembers.length === 0">
            <i>No members yet.</i>
          </mat-list-item>
        </mat-list>

        <div class="add-member-section">
          <h3>Add Member</h3>
          <form [formGroup]="addMemberForm" (ngSubmit)="addMember()">
            <mat-form-field appearance="fill" class="w-full">
              <mat-label>Select User</mat-label>
              <mat-select formControlName="userId">
                <mat-option *ngFor="let user of availableUsers" [value]="user.id">
                  {{ user.username }} ({{ user.email }})
                </mat-option>
              </mat-select>
            </mat-form-field>
            <button mat-raised-button color="primary" type="submit" 
                    [disabled]="addMemberForm.invalid || loading">
              Add Member
            </button>
          </form>
        </div>
      </div>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Close</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .members-container { min-width: 400px; }
    .header-actions { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
    .header-actions h3 { margin: 0; }
    .add-member-section { margin-top: 20px; padding-top: 20px; border-top: 1px solid #eee; }
    .w-full { width: 100%; }
    mat-list-item { display: flex; justify-content: space-between; align-items: center; }
  `]
})
export class ProjectMembersDialogComponent implements OnInit {
  addMemberForm: FormGroup;
  allUsers: User[] = [];
  currentMembers: User[] = [];
  availableUsers: User[] = [];
  loading = false;

  constructor(
    public dialogRef: MatDialogRef<ProjectMembersDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { project: Project },
    private fb: FormBuilder,
    private projectService: ProjectService,
    private userService: UserService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {
    this.addMemberForm = this.fb.group({
      userId: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    if (!this.data.project.id) return;
    // Load members
    this.projectService.getProject(this.data.project.id).subscribe({
      next: (project: any) => {
        // Members are returned in the project as either 'members' (full objects) or we can load from users
        this.currentMembers = project.members || [];
        this.userService.getUsers().subscribe(users => {
          const allUsersData = (users as any).content || users;
          this.allUsers = allUsersData;
          this.availableUsers = this.allUsers.filter(u => !this.currentMembers.find(cm => cm.id === u.id));
        });
      },
      error: (err) => {
        console.error('Error loading project:', err);
      }
    });
  }

  addMember() {
    if (this.addMemberForm.valid && this.data.project.id) {
      this.loading = true;
      const userId = this.addMemberForm.value.userId;
      this.projectService.addMemberToProject(this.data.project.id, userId).subscribe({
        next: () => {
          this.snackBar.open('Member added successfully', 'Close', { duration: 2000 });
          this.loadData();
          this.addMemberForm.reset();
          this.loading = false;
        },
        error: (err: any) => {
          this.snackBar.open('Failed to add member', 'Close', { duration: 3000 });
          this.loading = false;
        }
      });
    }
  }

  removeMember(user: User) {
    const projectId = this.data.project.id;
    if (projectId && user.id) {
        if(!confirm(`Remove ${user.username} from project?`)) return;

        this.loading = true;
        this.projectService.removeMemberFromProject(projectId, user.id).subscribe({
            next: () => {
                this.snackBar.open('Member removed successfully', 'Close', { duration: 2000 });
                this.loadData();
            },
            error: (err) => {
                console.error('Error removing member', err);
                this.snackBar.open('Failed to remove member', 'Close', { duration: 3000 });
                this.loading = false;
            }
        });
    }
  }

  openRoleAssignmentDialog() {
    const dialogRef = this.dialog.open(ProjectRoleAssignmentDialogComponent, {
      width: '600px',
      data: { project: this.data.project }
    });

    dialogRef.afterClosed().subscribe(() => {
      // Refresh data if needed
      this.loadData();
    });
  }
}
