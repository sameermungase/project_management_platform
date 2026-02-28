import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  // Use relative URL that works in both development and Docker environments
  private apiUrl = '/api/auth';
  private userSubject = new BehaviorSubject<any>(null);
  public user$ = this.userSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {
    this.loadUser();
  }

  loadUser() {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    if (token && user) {
      try {
        this.userSubject.next(JSON.parse(user));
      } catch (e) {
        this.userSubject.next({ token });
      }
    }
  }

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/signin`, credentials).pipe(
      tap((response: any) => {
        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify(response));
        this.userSubject.next(response);
      })
    );
  }

  register(userData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/signup`, userData);
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    this.userSubject.next(null);
    this.router.navigate(['/auth/login']);
  }

  get currentUserValue() {
    return this.userSubject.value;
  }
  
  getToken() {
      return localStorage.getItem('token');
  }

  hasRole(role: string): boolean {
    const user = this.currentUserValue;
    // Backend sends roles as plain strings without ROLE_ prefix (e.g., 'ADMIN', not 'ROLE_ADMIN')
    return user && user.roles && user.roles.includes(role);
  }

  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  isTechLead(): boolean {
    return this.hasRole('TECHNICAL_LEAD');
  }
}
