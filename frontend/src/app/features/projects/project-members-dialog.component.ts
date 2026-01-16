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
import { Project, ProjectService } from './project.service';
import { UserService, User } from '../users/user.service';

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
        <h3>Current Members</h3>
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
    private snackBar: MatSnackBar
  ) {
    this.addMemberForm = this.fb.group({
      userId: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.loading = true;
    // Load all users first
    this.userService.getUsers().subscribe({
      next: (users) => {
        this.allUsers = users;
        // Refresh project data to get latest members
        if (this.data.project.id) {
            this.projectService.getProject(this.data.project.id).subscribe({
                next: (project) => {
                    this.data.project = project;
                    this.updateMemberLists();
                    this.loading = false;
                },
                error: (err) => {
                    console.error('Error refreshing project', err);
                    this.loading = false;
                }
            });
        }
      },
      error: (err) => {
        console.error('Error loading users', err);
        this.snackBar.open('Failed to load users. You might not have permission.', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  updateMemberLists() {
    const memberIds = this.data.project.memberIds || [];
    this.currentMembers = this.allUsers.filter(u => memberIds.includes(u.id));
    this.availableUsers = this.allUsers.filter(u => !memberIds.includes(u.id));
  }

  addMember() {
    if (this.addMemberForm.invalid) return;
    
    const userId = this.addMemberForm.get('userId')?.value;
    const projectId = this.data.project.id;

    if (projectId && userId) {
      this.loading = true;
      this.projectService.addMemberToProject(projectId, userId).subscribe({
        next: () => {
          this.snackBar.open('Member added successfully', 'Close', { duration: 2000 });
          this.addMemberForm.reset();
          this.loadData(); // Reload to refresh lists
        },
        error: (err) => {
          console.error('Error adding member', err);
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
}
