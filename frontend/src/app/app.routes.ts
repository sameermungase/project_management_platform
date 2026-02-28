import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
    { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    { path: 'auth', loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES) },
    { path: 'dashboard', loadChildren: () => import('./features/dashboard/dashboard.routes').then(m => m.DASHBOARD_ROUTES) },
    { path: 'projects', loadChildren: () => import('./features/projects/project.routes').then(m => m.PROJECT_ROUTES) },
    { path: 'tasks', loadChildren: () => import('./features/tasks/task.routes').then(m => m.TASK_ROUTES) },
    { path: 'epics', loadChildren: () => import('./features/epics/epic.routes').then(m => m.EPIC_ROUTES), canActivate: [authGuard] },
    { path: 'milestones', loadChildren: () => import('./features/milestones/milestone.routes').then(m => m.MILESTONE_ROUTES), canActivate: [authGuard] },
    { path: 'approvals', loadChildren: () => import('./features/approvals/approval.routes').then(m => m.APPROVAL_ROUTES), canActivate: [authGuard] },
    { path: 'decisions', loadChildren: () => import('./features/decisions/decision.routes').then(m => m.DECISION_ROUTES), canActivate: [authGuard] },
    { path: 'templates', loadChildren: () => import('./features/templates/templates.routes').then(m => m.TEMPLATES_ROUTES), canActivate: [authGuard] },
    { path: 'users', loadComponent: () => import('./features/users/user-list/user-list.component').then(m => m.UserListComponent), canActivate: [authGuard] },
    { path: 'profile', loadComponent: () => import('./features/users/profile.component').then(m => m.ProfileComponent), canActivate: [authGuard] },
    { path: 'organization', loadChildren: () => import('./features/users/users.routes').then(m => m.USERS_ROUTES), canActivate: [authGuard] },
];
