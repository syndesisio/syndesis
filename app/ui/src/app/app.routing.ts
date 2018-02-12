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
    loadChildren: './integration/integration.module#IntegrationModule'
  },
  {
    path: 'connections',
    loadChildren:
      './connections/connections-routes.module#ConnectionsRoutesModule'
  },
  {
    path: 'customizations',
    loadChildren: './customizations/customizations.module#CustomizationsModule'
  },
  {
    path: 'settings',
    loadChildren: './settings/settings.module#SettingsModule'
  },
  {
    path: 'support',
    loadChildren: './support/support.module#SupportModule'
  }
];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule {}
