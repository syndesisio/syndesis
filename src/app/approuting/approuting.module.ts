import { NgModule } from '@angular/core';
import { RouterModule, Routes, PreloadAllModules } from '@angular/router';

const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', loadChildren: '../dashboard/dashboard.module#DashboardModule' },
  { path: 'integrations', loadChildren: '../integrations/integrations.module#IntegrationsModule' },
  { path: 'templates', loadChildren: '../templates/templates-routes.module#TemplateRoutesModule' },
  { path: 'connections', loadChildren: '../connections/connections.module#ConnectionsModule' },
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules }),
  ],
  exports: [
    RouterModule,
  ],
})
export class AppRoutingModule { }
