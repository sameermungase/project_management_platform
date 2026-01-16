import { Routes } from '@angular/router';
import { TaskListComponent } from './task-list.component';
import { TaskKanbanComponent } from './task-kanban.component';
import { MainLayoutComponent } from '../../core/main-layout.component';
import { authGuard } from '../../core/auth.guard';

import { TaskDetailComponent } from './task-detail.component';

export const TASK_ROUTES: Routes = [
    {
        path: '',
        component: MainLayoutComponent,
        canActivate: [authGuard],
        children: [
            { path: '', component: TaskListComponent },
            { path: 'kanban', component: TaskKanbanComponent },
            { path: ':id', component: TaskDetailComponent }
        ]
    }
];
