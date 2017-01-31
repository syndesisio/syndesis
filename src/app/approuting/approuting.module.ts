import { NgModule } from '@angular/core';
import { RouterModule, Routes, PreloadAllModules } from '@angular/router';

import { AuthGuard } from './auth-guard.service';

const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full', canActivate: [AuthGuard] },
  { path: 'dashboard', loadChildren: '../dashboard/dashboard.module#DashboardModule', canActivate: [AuthGuard] },
  { path: 'integrations', loadChildren: '../integrations/integrations.module#IntegrationsModule', canActivate: [AuthGuard] },
  { path: 'templates', loadChildren: '../templates/templates-routes.module#TemplateRoutesModule', canActivate: [AuthGuard] },
  { path: 'connections', loadChildren: '../connections/connections-routes.module#ConnectionsRoutesModule', canActivate: [AuthGuard] },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules }),
  ],
  exports: [
    RouterModule,
  ],
  providers: [
    AuthGuard,
  ],
})
export class AppRoutingModule { }
