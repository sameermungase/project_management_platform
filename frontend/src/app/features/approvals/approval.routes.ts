import { Routes } from '@angular/router';
import { ApprovalQueueComponent } from './approval-queue.component';
import { MainLayoutComponent } from '../../core/main-layout.component';
import { authGuard } from '../../core/auth.guard';

export const APPROVAL_ROUTES: Routes = [
    {
        path: '',
        component: MainLayoutComponent,
        canActivate: [authGuard],
        children: [
            { path: '', component: ApprovalQueueComponent }
        ]
    }
];
