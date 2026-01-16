import { Routes } from '@angular/router';
import { ProjectListComponent } from './project-list.component';
import { MainLayoutComponent } from '../../core/main-layout.component';
import { authGuard } from '../../core/auth.guard';

export const PROJECT_ROUTES: Routes = [
    {
        path: '',
        component: MainLayoutComponent,
        canActivate: [authGuard],
        children: [
            { path: '', component: ProjectListComponent }
        ]
    }
];
