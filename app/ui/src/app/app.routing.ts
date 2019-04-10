import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadChildren: './dashboard/dashboard.module#DashboardModule'
  },
  { path: 'dashboard', redirectTo: '', pathMatch: 'full' },
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
