import { Component, Input, OnInit } from '@angular/core';
import { SharedModule } from '../../shared/shared.module';
import { CommentService, Comment, CommentRequest } from './comment.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-comment-list',
  standalone: true,
  imports: [SharedModule, CommonModule],
  template: `
    <div class="comment-section">
      <div class="comment-header">
        <h3>
          <mat-icon>comment</mat-icon>
          Comments ({{ comments.length }})
        </h3>
        <button mat-raised-button color="primary" (click)="showAddComment = !showAddComment">
          <mat-icon>add</mat-icon> Add Comment
        </button>
      </div>

      <div *ngIf="showAddComment" class="comment-form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Write a comment...</mat-label>
          <textarea matInput [(ngModel)]="newComment.content" rows="3" placeholder="Type your comment here"></textarea>
        </mat-form-field>
        <div class="form-actions">
          <button mat-button (click)="showAddComment = false">Cancel</button>
          <button mat-raised-button color="primary" (click)="addComment()" [disabled]="!newComment.content.trim()">
            Post Comment
          </button>
        </div>
      </div>

      <div class="comments-container">
        <div *ngFor="let comment of comments" class="comment-item">
          <mat-card>
            <mat-card-header>
              <div class="comment-header-info">
                <div class="user-info">
                  <mat-icon class="avatar">account_circle</mat-icon>
                  <div>
                    <strong>{{ comment.username }}</strong>
                    <span class="timestamp">{{ comment.createdAt | date:'short' }}</span>
                  </div>
                </div>
                <div class="comment-actions" *ngIf="comment.canEdit || comment.canDelete">
                  <button mat-icon-button *ngIf="comment.canEdit" (click)="editComment(comment)">
                    <mat-icon>edit</mat-icon>
                  </button>
                  <button mat-icon-button *ngIf="comment.canDelete" (click)="deleteComment(comment.id!)" color="warn">
                    <mat-icon>delete</mat-icon>
                  </button>
                </div>
              </div>
            </mat-card-header>
            <mat-card-content>
              <div *ngIf="!comment.editing" class="comment-content">
                <p>{{ comment.content }}</p>
              </div>
              <div *ngIf="comment.editing" class="edit-form">
                <mat-form-field appearance="outline" class="full-width">
                  <textarea matInput [(ngModel)]="comment.editContent" rows="2"></textarea>
                </mat-form-field>
                <div class="form-actions">
                  <button mat-button (click)="cancelEdit(comment)">Cancel</button>
                  <button mat-raised-button color="primary" (click)="saveComment(comment)">Save</button>
                </div>
              </div>
              
              <div class="reactions" *ngIf="comment.reactions && comment.reactions.length > 0">
                <mat-chip-set>
                  <mat-chip *ngFor="let reaction of comment.reactions" (click)="toggleReaction(comment, reaction.emoji)">
                    {{ reaction.emoji }} {{ getReactionCount(comment, reaction.emoji) }}
                  </mat-chip>
                </mat-chip-set>
              </div>

              <div class="comment-footer">
                <button mat-button (click)="toggleReaction(comment, '👍')">
                  <mat-icon>thumb_up</mat-icon> Like
                </button>
                <button mat-button (click)="toggleReaction(comment, '❤️')">
                  <mat-icon>favorite</mat-icon> Love
                </button>
                <button mat-button (click)="replyToComment(comment)">
                  <mat-icon>reply</mat-icon> Reply
                </button>
              </div>

              <!-- Replies -->
              <div *ngIf="comment.replies && comment.replies.length > 0" class="replies">
                <div *ngFor="let reply of comment.replies" class="reply-item">
                  <mat-card class="reply-card">
                    <div class="reply-header">
                      <mat-icon class="small-avatar">account_circle</mat-icon>
                      <strong>{{ reply.username }}</strong>
                      <span class="timestamp">{{ reply.createdAt | date:'short' }}</span>
                    </div>
                    <p>{{ reply.content }}</p>
                  </mat-card>
                </div>
              </div>

              <!-- Reply Form -->
              <div *ngIf="comment.showReplyForm" class="reply-form">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Write a reply...</mat-label>
                  <textarea matInput [(ngModel)]="comment.replyContent" rows="2"></textarea>
                </mat-form-field>
                <div class="form-actions">
                  <button mat-button (click)="comment.showReplyForm = false">Cancel</button>
                  <button mat-raised-button color="primary" (click)="postReply(comment)" [disabled]="!comment.replyContent?.trim()">
                    Post Reply
                  </button>
                </div>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <div *ngIf="comments.length === 0" class="no-comments">
          <mat-icon>comment</mat-icon>
          <p>No comments yet. Be the first to comment!</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .comment-section { padding: 20px; }
    .comment-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
    .comment-header h3 { display: flex; align-items: center; gap: 10px; }
    .comment-form { margin-bottom: 20px; padding: 15px; background: #f5f5f5; border-radius: 8px; }
    .full-width { width: 100%; }
    .form-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 10px; }
    .comments-container { display: flex; flex-direction: column; gap: 15px; }
    .comment-item { width: 100%; }
    .comment-header-info { display: flex; justify-content: space-between; width: 100%; }
    .user-info { display: flex; align-items: center; gap: 10px; }
    .avatar { font-size: 40px; width: 40px; height: 40px; color: #666; }
    .timestamp { color: #999; font-size: 0.85em; margin-left: 10px; }
    .comment-actions { display: flex; gap: 5px; }
    .comment-content p { margin: 10px 0; line-height: 1.6; }
    .reactions { margin: 10px 0; }
    .comment-footer { display: flex; gap: 10px; margin-top: 10px; padding-top: 10px; border-top: 1px solid #eee; }
    .replies { margin-top: 15px; margin-left: 40px; }
    .reply-item { margin-bottom: 10px; }
    .reply-card { background: #f9f9f9; }
    .reply-header { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
    .small-avatar { font-size: 24px; width: 24px; height: 24px; }
    .reply-form { margin-top: 10px; padding: 10px; background: #f5f5f5; border-radius: 4px; }
    .no-comments { text-align: center; padding: 40px; color: #999; }
    .no-comments mat-icon { font-size: 48px; width: 48px; height: 48px; margin-bottom: 10px; }
  `]
})
export class CommentListComponent implements OnInit {
  @Input() taskId?: string;
  @Input() projectId?: string;
  
  comments: (Comment & { editing?: boolean; editContent?: string; showReplyForm?: boolean; replyContent?: string })[] = [];
  showAddComment = false;
  newComment: CommentRequest = { content: '' };

  constructor(private commentService: CommentService) {}

  ngOnInit() {
    this.loadComments();
  }

  loadComments() {
    if (this.taskId) {
      this.commentService.getCommentsByTask(this.taskId).subscribe({
        next: (data) => {
          this.comments = data;
        },
        error: (err) => {
          console.error('Error loading comments:', err);
        }
      });
    } else if (this.projectId) {
      this.commentService.getCommentsByProject(this.projectId).subscribe({
        next: (data) => {
          this.comments = data;
        },
        error: (err) => {
          console.error('Error loading comments:', err);
        }
      });
    }
  }

  addComment() {
    const request: CommentRequest = {
      content: this.newComment.content,
      taskId: this.taskId,
      projectId: this.projectId
    };
    this.commentService.createComment(request).subscribe({
      next: () => {
        this.newComment.content = '';
        this.showAddComment = false;
        this.loadComments();
      },
      error: (err) => {
        console.error('Error creating comment:', err);
      }
    });
  }

  editComment(comment: any) {
    comment.editing = true;
    comment.editContent = comment.content;
  }

  cancelEdit(comment: any) {
    comment.editing = false;
    comment.editContent = '';
  }

  saveComment(comment: any) {
    const request: CommentRequest = {
      content: comment.editContent,
      taskId: this.taskId,
      projectId: this.projectId
    };
    this.commentService.updateComment(comment.id, request).subscribe({
      next: () => {
        comment.editing = false;
        this.loadComments();
      },
      error: (err) => {
        console.error('Error updating comment:', err);
      }
    });
  }

  deleteComment(commentId: string) {
    if (confirm('Are you sure you want to delete this comment?')) {
      this.commentService.deleteComment(commentId).subscribe({
        next: () => {
          this.loadComments();
        },
        error: (err) => {
          console.error('Error deleting comment:', err);
        }
      });
    }
  }

  toggleReaction(comment: Comment, emoji: string) {
    const hasReaction = comment.reactions?.some(r => r.emoji === emoji);
    if (hasReaction) {
      this.commentService.removeReaction(comment.id!, emoji).subscribe({
        next: () => {
          this.loadComments();
        },
        error: (err) => {
          console.error('Error removing reaction:', err);
        }
      });
    } else {
      this.commentService.addReaction(comment.id!, emoji).subscribe({
        next: () => {
          this.loadComments();
        },
        error: (err) => {
          console.error('Error adding reaction:', err);
        }
      });
    }
  }

  getReactionCount(comment: Comment, emoji: string): number {
    return comment.reactions?.filter(r => r.emoji === emoji).length || 0;
  }

  replyToComment(comment: any) {
    comment.showReplyForm = !comment.showReplyForm;
    if (!comment.showReplyForm) {
      comment.replyContent = '';
    }
  }

  postReply(comment: any) {
    const request: CommentRequest = {
      content: comment.replyContent,
      taskId: this.taskId,
      projectId: this.projectId,
      parentCommentId: comment.id
    };
    this.commentService.createComment(request).subscribe({
      next: () => {
        comment.showReplyForm = false;
        comment.replyContent = '';
        this.loadComments();
      },
      error: (err) => {
        console.error('Error creating reply:', err);
      }
    });
  }
}
