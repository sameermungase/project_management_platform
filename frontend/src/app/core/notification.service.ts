import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface Notification {
  id: string;
  userId: string;
  type: string;
  title: string;
  message: string;
  entityType?: string;
  entityId?: string;
  isRead: boolean;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private apiUrl = '/api/notifications';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  getNotifications(userId: string): Observable<Notification[]> {
    const params = new HttpParams().set('userId', userId);
    return this.http.get<Notification[]>(this.apiUrl, { params });
  }

  getNotificationsPaginated(userId: string, page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('userId', userId)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/page`, { params });
  }

  getUnreadNotifications(userId: string): Observable<Notification[]> {
    const params = new HttpParams().set('userId', userId);
    return this.http.get<Notification[]>(`${this.apiUrl}/unread`, { params });
  }

  getUnreadCount(userId: string): Observable<number> {
    const params = new HttpParams().set('userId', userId);
    return this.http.get<number>(`${this.apiUrl}/unread/count`, { params });
  }

  markAsRead(notificationId: string, userId: string): Observable<void> {
    const params = new HttpParams().set('userId', userId);
    return this.http.put<void>(`${this.apiUrl}/${notificationId}/read`, null, { params });
  }

  markAllAsRead(userId: string): Observable<void> {
    const params = new HttpParams().set('userId', userId);
    return this.http.put<void>(`${this.apiUrl}/read-all`, null, { params });
  }
}
