import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Comment {
  id?: string;
  content: string;
  taskId?: string;
  projectId?: string;
  parentCommentId?: string;
  userId?: string;
  username?: string;
  userEmail?: string;
  replies?: Comment[];
  reactions?: Reaction[];
  createdAt?: string;
  updatedAt?: string;
  canEdit?: boolean;
  canDelete?: boolean;
}

export interface Reaction {
  id?: string;
  emoji: string;
  userId?: string;
  username?: string;
  createdAt?: string;
}

export interface CommentRequest {
  content: string;
  taskId?: string;
  projectId?: string;
  parentCommentId?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private apiUrl = '/api/comments';

  constructor(private http: HttpClient) {}

  getCommentsByTask(taskId: string): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.apiUrl}/task/${taskId}`);
  }

  getCommentsByProject(projectId: string): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.apiUrl}/project/${projectId}`);
  }

  createComment(comment: CommentRequest): Observable<Comment> {
    return this.http.post<Comment>(this.apiUrl, comment);
  }

  updateComment(commentId: string, comment: CommentRequest): Observable<Comment> {
    return this.http.put<Comment>(`${this.apiUrl}/${commentId}`, comment);
  }

  deleteComment(commentId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${commentId}`);
  }

  addReaction(commentId: string, emoji: string): Observable<Comment> {
    return this.http.post<Comment>(`${this.apiUrl}/${commentId}/reactions`, null, {
      params: { emoji }
    });
  }

  removeReaction(commentId: string, emoji: string): Observable<Comment> {
    return this.http.delete<Comment>(`${this.apiUrl}/${commentId}/reactions`, {
      params: { emoji }
    });
  }
}
