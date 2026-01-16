import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService, User } from '../user.service';
import { AuthService } from '../../../core/auth.service';
import { SharedModule } from '../../../shared/shared.module';
import { MatDialog } from '@angular/material/dialog';
import { RoleEditDialogComponent } from '../role-edit-dialog.component';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, SharedModule],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.scss'
})
export class UserListComponent implements OnInit {
  users: User[] = [];
  currentUserRoles: string[] = [];
  displayedColumns: string[] = ['username', 'email', 'roles', 'actions'];

  constructor(
    private userService: UserService, 
    private authService: AuthService,
    private dialog: MatDialog
  ) {}

  ngOnInit() {
    this.loadUsers();
    this.getCurrentUserRoles();
  }

  loadUsers() {
    this.userService.getUsers().subscribe({
      next: (data) => this.users = data,
      error: (err) => console.error('Error fetching users', err)
    });
  }

  getCurrentUserRoles() {
    // Assuming token has roles or we fetch them. For now, checking authService
    // We might need to decode token here if it's not in user object
    const user = this.authService.currentUserValue;
    if (user && user.roles) {
      this.currentUserRoles = user.roles;
    } else {
        // Fallback: If roles are not in currentUserValue (which is just token often), 
        // we should ideally fetch profile or decode token.
        // For simplicity in this demo, we might rely on the backend to enforce permissions mostly,
        // but for UI hiding, let's assume we can get it.
        // If roles are in JWT, we can parse it.
        const token = this.authService.getToken();
        if (token) {
            const payload = JSON.parse(atob(token.split('.')[1]));
            this.currentUserRoles = payload.roles || []; // Assuming 'roles' key in JWT
        }
    }
  }

  canDelete(targetUser: User): boolean {
    const isAdmin = this.currentUserRoles.includes('ROLE_ADMIN') || this.currentUserRoles.includes('ADMIN');
    const isTL = this.currentUserRoles.includes('ROLE_TECHNICAL_LEAD') || this.currentUserRoles.includes('TECHNICAL_LEAD');
    
    // Admin can delete anyone (except maybe themselves, but backend handles that/logic here)
    if (isAdmin) return true;

    // TL can delete only USERS
    if (isTL) {
        return targetUser.roles.length === 1 && targetUser.roles.includes('USER');
    }

    return false;
  }

  canEdit(targetUser: User): boolean {
    const isAdmin = this.currentUserRoles.includes('ROLE_ADMIN') || this.currentUserRoles.includes('ADMIN');
    const isTL = this.currentUserRoles.includes('ROLE_TECHNICAL_LEAD') || this.currentUserRoles.includes('TECHNICAL_LEAD');
    
    if (isAdmin) return true;

    if (isTL) {
        return targetUser.roles.length === 1 && targetUser.roles.includes('USER');
    }

    return false;
  }

  editUser(user: User) {
    const dialogRef = this.dialog.open(RoleEditDialogComponent, {
      width: '400px',
      data: { user }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadUsers();
      }
    });
  }

  deleteUser(user: User) {
    if (confirm(`Are you sure you want to delete ${user.username}?`)) {
      this.userService.deleteUser(user.id).subscribe({
        next: () => {
            this.loadUsers(); // Refresh list
            alert('User deleted successfully');
        },
        error: (err) => {
            alert(err.error?.message || 'Failed to delete user');
        }
      });
    }
  }
}
