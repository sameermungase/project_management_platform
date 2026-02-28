import { Routes } from '@angular/router';
import { DecisionLogComponent } from './decision-log.component';
import { MainLayoutComponent } from '../../core/main-layout.component';
import { authGuard } from '../../core/auth.guard';

export const DECISION_ROUTES: Routes = [
    {
        path: '',
        component: MainLayoutComponent,
        canActivate: [authGuard],
        children: [
            { path: '', component: DecisionLogComponent }
        ]
    }
];
