import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  {
    path: 'dashboard',
    loadChildren: './dashboard/dashboard.module#DashboardModule'
  },
  {
    path: 'integrations',
    loadChildren: './integrations/integrations.module#IntegrationsModule'
  },
  {
    path: 'templates',
    loadChildren: './templates/templates-routes.module#TemplateRoutesModule'
  },
  {
    path: 'connections',
    loadChildren:
      './connections/connections-routes.module#ConnectionsRoutesModule'
  },
  {
    path: 'settings',
    loadChildren: './settings/settings.module#SettingsModule'
  },
  {
    path: 'customizations',
    loadChildren: './customizations/customizations.module#CustomizationsModule'
  }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule {}
