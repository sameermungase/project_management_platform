import { Routes } from '@angular/router';
import { MilestoneListComponent } from './milestone-list.component';
import { MainLayoutComponent } from '../../core/main-layout.component';
import { authGuard } from '../../core/auth.guard';

export const MILESTONE_ROUTES: Routes = [
    {
        path: '',
        component: MainLayoutComponent,
        canActivate: [authGuard],
        children: [
            { path: '', component: MilestoneListComponent }
        ]
    }
];
