import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TaskService } from './task.service';
import { SharedModule } from '../../shared/shared.module';

@Component({
  selector: 'app-task-form',
  standalone: true,
  imports: [SharedModule],
  template: `
    <h2 mat-dialog-title>{{ data.task.id ? 'Edit' : 'New' }} Task</h2>
    <mat-dialog-content>
      <form [formGroup]="form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Title</mat-label>
          <input matInput formControlName="title">
        </mat-form-field>
        
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Description</mat-label>
          <textarea matInput formControlName="description" rows="3"></textarea>
        </mat-form-field>

        <div class="row">
            <mat-form-field appearance="outline">
            <mat-label>Status</mat-label>
            <mat-select formControlName="status">
                <mat-option value="TODO">To Do</mat-option>
                <mat-option value="IN_PROGRESS">In Progress</mat-option>
                <mat-option value="DONE">Done</mat-option>
            </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline">
            <mat-label>Priority</mat-label>
            <mat-select formControlName="priority">
                <mat-option value="LOW">Low</mat-option>
                <mat-option value="MEDIUM">Medium</mat-option>
                <mat-option value="HIGH">High</mat-option>
            </mat-select>
            </mat-form-field>
        </div>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Due Date</mat-label>
          <input matInput [matDatepicker]="picker" formControlName="dueDate">
          <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
          <mat-datepicker #picker></mat-datepicker>
        </mat-form-field>

      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close>Cancel</button>
      <button mat-raised-button color="primary" [disabled]="form.invalid" (click)="save()">Save</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .full-width { width: 100%; margin-bottom: 10px; }
    .row { display: flex; gap: 10px; }
    .row mat-form-field { flex: 1; }
  `]
})
export class TaskFormComponent {
  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService,
    public dialogRef: MatDialogRef<TaskFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.form = this.fb.group({
      title: [data.task.title || '', Validators.required],
      description: [data.task.description || ''],
      status: [data.task.status || 'TODO', Validators.required],
      priority: [data.task.priority || 'MEDIUM', Validators.required],
      dueDate: [data.task.dueDate || new Date(), Validators.required],
      projectId: [data.projectId, Validators.required]
    });
  }

  save() {
    if (this.form.valid) {
      const task = { ...this.data.task, ...this.form.value };
      // Ensure date is ISO string if needed by backend, or just Date object if mapper handles it
      // Simple fix: conversion usually handled by JSON.stringify but let's be safe if backend expects specific format
      
      if (task.id) {
        this.taskService.updateTask(task.id, task).subscribe(() => this.dialogRef.close(true));
      } else {
        this.taskService.createTask(task).subscribe(() => this.dialogRef.close(true));
      }
    }
  }
}
