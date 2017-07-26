import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { DynamicFormsCoreModule } from '@ng2-dynamic-forms/core';
import { DynamicFormsBootstrapUIModule } from '@ng2-dynamic-forms/ui-bootstrap';
import { ModalModule } from 'ngx-bootstrap';
import { ListModule, ToolbarModule } from 'patternfly-ng';
import { SyndesisCommonModule } from '../common/common.module';
import { DynamicFormsPatternflyUIModule } from '../common/ui-patternfly/ui-patternfly.module';

import { SettingsRootComponent } from './settings-root.component';
import { OAuthAppsComponent } from './oauth-apps/oauth-apps.component';
import { OAuthAppFormComponent } from './oauth-apps/oauth-app-form.component';
import { OAuthAppModal } from './oauth-apps/oauth-app-modal.component';

const routes: Routes = [
  {
    path: '',
    component: SettingsRootComponent,
    children: [
      {
        path: 'oauth-clients',
        component: OAuthAppsComponent,
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
    DynamicFormsPatternflyUIModule,
    ListModule,
    ToolbarModule,
    ModalModule,
    RouterModule.forChild(routes),
    SyndesisCommonModule,
  ],
  exports: [],
  declarations: [
    SettingsRootComponent,
    OAuthAppsComponent,
    OAuthAppFormComponent,
    OAuthAppModal,
  ],
  providers: [],
})
export class SettingsModule {}
