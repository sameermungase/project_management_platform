import { Component, OnInit, OnDestroy } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { NotificationService, Notification } from '../../core/notification.service';
import { AuthService } from '../../core/auth.service';
import { Subject, interval } from 'rxjs';
import { takeUntil, switchMap } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-notification-badge',
  standalone: true,
  imports: [SharedModule],
  template: `
    <div class="notification-container" *ngIf="userId">
      <button 
        mat-icon-button 
        [matMenuTriggerFor]="notificationMenu"
        class="notification-button"
        [class.has-unread]="unreadCount > 0">
        <mat-icon>notifications</mat-icon>
        <span class="badge" *ngIf="unreadCount > 0">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
      </button>

      <mat-menu #notificationMenu="matMenu" class="notification-menu" (menuOpened)="onMenuOpen()">
        <div class="notification-header">
          <h3>Notifications</h3>
          <button mat-icon-button (click)="markAllAsRead()" [disabled]="unreadCount === 0" matTooltip="Mark all as read">
            <mat-icon>done_all</mat-icon>
          </button>
        </div>

        <mat-divider></mat-divider>

        <div class="notification-list" *ngIf="notifications.length > 0; else noNotifications">
          <button 
            *ngFor="let notification of notifications | slice:0:5"
            mat-menu-item 
            class="notification-item"
            [class.unread]="!notification.isRead"
            (click)="handleNotificationClick(notification)">
            <div class="notification-content">
              <div class="notification-title">
                <strong>{{ notification.title }}</strong>
                <span class="unread-indicator" *ngIf="!notification.isRead">●</span>
              </div>
              <p class="notification-message">{{ notification.message }}</p>
              <small class="notification-time">{{ notification.createdAt | date:'short' }}</small>
            </div>
            <button 
              mat-icon-button 
              (click)="deleteNotification(notification.id, $event)"
              matTooltip="Delete">
              <mat-icon class="delete-icon">close</mat-icon>
            </button>
          </button>
        </div>

        <ng-template #noNotifications>
          <div class="no-notifications">
            <mat-icon>notifications_none</mat-icon>
            <p>No notifications</p>
          </div>
        </ng-template>

        <mat-divider *ngIf="notifications.length > 0"></mat-divider>

        <button 
          mat-menu-item 
          *ngIf="notifications.length > 0"
          class="view-all-button">
          View All Notifications
        </button>
      </mat-menu>
    </div>
  `,
  styles: [`
    .notification-container {
      position: relative;
    }

    .notification-button {
      position: relative;
    }

    .notification-button.has-unread {
      color: #ff5252;
    }

    .badge {
      position: absolute;
      top: -5px;
      right: -5px;
      background-color: #ff5252;
      color: white;
      border-radius: 50%;
      width: 20px;
      height: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 10px;
      font-weight: bold;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    }

    .notification-menu {
      min-width: 350px !important;
      max-width: 400px !important;
    }

    .notification-header {
      padding: 12px 16px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .notification-header h3 {
      margin: 0;
      font-size: 16px;
      font-weight: 500;
    }

    .notification-list {
      max-height: 400px;
      overflow-y: auto;
    }

    .notification-item {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 12px 16px !important;
      text-align: left;
      width: 100%;
      border-radius: 0;
      border-left: 3px solid transparent;
      min-height: auto;
      height: auto;
      white-space: normal;
      line-height: normal;
    }

    .notification-item.unread {
      background-color: #f5f5f5;
      border-left-color: #3f51b5;
    }

    .notification-content {
      flex: 1;
      width: 100%;
    }

    .notification-title {
      display: flex;
      gap: 8px;
      align-items: center;
      margin-bottom: 4px;
    }

    .unread-indicator {
      color: #ff5252;
      font-size: 12px;
    }

    .notification-message {
      margin: 4px 0;
      color: #666;
      font-size: 12px;
      line-height: 1.4;
    }

    .notification-time {
      color: #999;
      font-size: 11px;
    }

    .delete-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }

    .no-notifications {
      padding: 30px 16px;
      text-align: center;
      color: #999;
    }

    .no-notifications mat-icon {
      font-size: 40px;
      width: 40px;
      height: 40px;
      margin-bottom: 10px;
      opacity: 0.5;
    }

    .no-notifications p {
      margin: 0;
      font-size: 14px;
    }

    .view-all-button {
      padding: 12px 16px !important;
      text-align: center;
      color: #3f51b5;
      font-weight: 500;
      width: 100%;
    }
  `]
})
export class NotificationBadgeComponent implements OnInit, OnDestroy {
  userId: string = '';
  notifications: Notification[] = [];
  unreadCount: number = 0;
  private destroy$ = new Subject<void>();

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    const user = this.authService.currentUserValue;
    if (user && user.id) {
      this.userId = user.id;
      this.loadNotifications();
      this.startPolling();
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // Method called when menu is opened
  onMenuOpen() {
    this.loadNotifications();
  }

  private loadNotifications() {
    if (!this.userId) return;

    this.notificationService.getNotificationsPaginated(this.userId, 0, 10).subscribe({
      next: (response) => {
        this.notifications = response.content || [];
        this.updateUnreadCount();
      },
      error: (error) => {
        console.error('Failed to load notifications:', error);
        this.notifications = [];
      }
    });
  }

  private updateUnreadCount() {
    if (!this.userId) return;

    this.notificationService.getUnreadCount(this.userId).subscribe({
      next: (count) => {
        this.unreadCount = count;
      },
      error: (error) => {
        console.error('Failed to get unread count:', error);
        this.unreadCount = 0;
      }
    });
  }

  private startPolling() {
    interval(30000) // Poll every 30 seconds
      .pipe(
        switchMap(() => this.notificationService.getUnreadCount(this.userId)),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (count) => {
          this.unreadCount = count;
          // Reload notifications if count changed
          if (count > 0) {
            this.loadNotifications();
          }
        }
      });
  }

  markAllAsRead() {
    if (!this.userId) return;

    this.notificationService.markAllAsRead(this.userId).subscribe({
      next: () => {
        this.snackBar.open('All notifications marked as read', 'Close', { duration: 2000 });
        this.loadNotifications();
      },
      error: (error) => {
        console.error('Failed to mark all as read:', error);
      }
    });
  }

  handleNotificationClick(notification: Notification) {
    if (!notification.isRead && this.userId) {
      this.notificationService.markAsRead(notification.id, this.userId).subscribe({
        next: () => {
          this.loadNotifications();
        },
        error: (error) => {
          console.error('Failed to mark as read:', error);
        }
      });
    }
  }

  deleteNotification(notificationId: string, event: Event) {
    event.stopPropagation();
    // Currently there's no delete endpoint in the backend,
    // so we'll just remove it from the UI
    this.notifications = this.notifications.filter(n => n.id !== notificationId);
  }
}
