import { Component } from '@angular/core';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { PermissionService } from '../core/permission.service';
import { SharedModule } from '../shared/shared.module';
import { Observable, of } from 'rxjs';
import { NotificationBadgeComponent } from '../features/notifications/notification-badge.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [SharedModule, NotificationBadgeComponent, CommonModule],
  template: `
    <mat-sidenav-container class="sidenav-container">
      <mat-sidenav #drawer class="sidenav" fixedInViewport
          [attr.role]="(isHandset$ | async) ? 'dialog' : 'navigation'"
          [mode]="(isHandset$ | async) ? 'over' : 'side'"
          [opened]="(isHandset$ | async) === false">
        <mat-toolbar>Smart Project</mat-toolbar>
        <mat-nav-list>
          <a mat-list-item routerLink="/dashboard">
            <mat-icon matListItemIcon>dashboard</mat-icon>
            <span matListItemTitle>Dashboard</span>
          </a>
          <a mat-list-item routerLink="/projects">
            <mat-icon matListItemIcon>folder</mat-icon>
            <span matListItemTitle>Projects</span>
          </a>
          <a mat-list-item routerLink="/tasks">
            <mat-icon matListItemIcon>assignment</mat-icon>
            <span matListItemTitle>Tasks</span>
          </a>
          <a mat-list-item routerLink="/epics">
            <mat-icon matListItemIcon>stars</mat-icon>
            <span matListItemTitle>Epics</span>
          </a>
          <a mat-list-item routerLink="/milestones">
            <mat-icon matListItemIcon>flag</mat-icon>
            <span matListItemTitle>Milestones</span>
          </a>
          
          <!-- Role-Based Visibility: Approvals -->
          <a mat-list-item routerLink="/approvals" *ngIf="canViewApprovals()">
            <mat-icon matListItemIcon>approval</mat-icon>
            <span matListItemTitle>Approvals</span>
          </a>
          
          <!-- Role-Based Visibility: Decisions -->
          <a mat-list-item routerLink="/decisions" *ngIf="canViewDecisions()">
            <mat-icon matListItemIcon>gavel</mat-icon>
            <span matListItemTitle>Decisions</span>
          </a>
          
          <a mat-list-item routerLink="/profile">
            <mat-icon matListItemIcon>person</mat-icon>
            <span matListItemTitle>Profile</span>
          </a>
          <a mat-list-item routerLink="/organization" *ngIf="isAdmin()">
            <mat-icon matListItemIcon>business</mat-icon>
            <span matListItemTitle>Organization</span>
          </a>
        </mat-nav-list>
      </mat-sidenav>
      <mat-sidenav-content>
        <mat-toolbar color="primary">
          <button
            type="button"
            aria-label="Toggle sidenav"
            mat-icon-button
            (click)="drawer.toggle()"
            *ngIf="isHandset$ | async">
            <mat-icon>menu</mat-icon>
          </button>
          <span>Application</span>
          <span class="spacer"></span>
          <app-notification-badge></app-notification-badge>
          <button mat-icon-button (click)="logout()">
            <mat-icon>logout</mat-icon>
          </button>
        </mat-toolbar>
        <div class="content">
            <router-outlet></router-outlet>
        </div>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: [`
    .sidenav-container { height: 100%; }
    .sidenav { width: 200px; }
    .spacer { flex: 1 1 auto; }
    .content { padding: 20px; }
  `]
})
export class MainLayoutComponent {
  // Observable for handset detection - returns false for desktop view
  isHandset$: Observable<boolean> = of(false);

  constructor(
    private authService: AuthService,
    private permissionService: PermissionService
  ) {}

  logout() {
    this.authService.logout();
  }

  isAdmin(): boolean {
    const user = this.authService.currentUserValue;
    return user && user.roles && user.roles.includes('ADMIN');
  }

  canViewApprovals(): boolean {
    return this.permissionService.canViewApprovals();
  }

  canViewDecisions(): boolean {
    return this.permissionService.canViewDecisions();
  }
}
