import { Routes } from '@angular/router';
import { EpicListComponent } from './epic-list.component';
import { MainLayoutComponent } from '../../core/main-layout.component';
import { authGuard } from '../../core/auth.guard';

export const EPIC_ROUTES: Routes = [
    {
        path: '',
        component: MainLayoutComponent,
        canActivate: [authGuard],
        children: [
            { path: '', component: EpicListComponent }
        ]
    }
];
