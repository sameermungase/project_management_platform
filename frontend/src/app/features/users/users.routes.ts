import { Routes } from '@angular/router';
import { OrganizationComponent } from './organization.component';
import { MainLayoutComponent } from '../../core/main-layout.component';
import { authGuard } from '../../core/auth.guard';

export const USERS_ROUTES: Routes = [
    {
        path: '',
        component: MainLayoutComponent,
        canActivate: [authGuard],
        children: [
            { path: '', component: OrganizationComponent }
        ]
    }
];
