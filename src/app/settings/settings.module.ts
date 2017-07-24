import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { DynamicFormsCoreModule } from '@ng2-dynamic-forms/core';
import { DynamicFormsBootstrapUIModule } from '@ng2-dynamic-forms/ui-bootstrap';
import { SyndesisCommonModule } from '../common/common.module';

import { SettingsRootComponent } from './settings-root.component';
import { OAuthClientsComponent } from './oauth-clients/oauth-clients.component';

const routes: Routes = [
  {
    path: '',
    component: SettingsRootComponent,
    children: [
      {
        path: 'oauth-clients',
        component: OAuthClientsComponent,
      },
      {
        path: '',
        redirectTo: 'oauth-clients',
      },
    ],
  },
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DynamicFormsCoreModule,
    DynamicFormsBootstrapUIModule,
    RouterModule.forChild(routes),
    SyndesisCommonModule,
  ],
  exports: [],
  declarations: [SettingsRootComponent, OAuthClientsComponent],
  providers: [],
})
export class SettingsModule {}
