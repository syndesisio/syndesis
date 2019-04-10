import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { DynamicFormsCoreModule } from '@ng-dynamic-forms/core';
import { SyndesisCommonModule } from '@syndesis/ui/common/common.module';
import { PatternflyUIModule } from '@syndesis/ui/common/ui-patternfly/ui-patternfly.module';
import { VendorModule } from '@syndesis/ui/vendor';

import { SettingsRootComponent } from '@syndesis/ui/settings/settings-root.component';
import { OAuthAppsComponent } from '@syndesis/ui/settings/oauth-apps/oauth-apps.component';
import { OAuthAppFormComponent } from '@syndesis/ui/settings/oauth-apps/oauth-app-form.component';
import { OAuthAppModalComponent } from '@syndesis/ui/settings/oauth-apps/oauth-app-modal.component';

const routes: Routes = [
  {
    path: '',
    component: SettingsRootComponent,
    children: [
      {
        path: 'oauth-clients',
        component: OAuthAppsComponent
      },
      {
        path: '',
        redirectTo: 'oauth-clients'
      }
    ]
  }
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    DynamicFormsCoreModule,
    PatternflyUIModule,
    RouterModule.forChild(routes),
    VendorModule,
    SyndesisCommonModule
  ],
  exports: [],
  declarations: [
    SettingsRootComponent,
    OAuthAppsComponent,
    OAuthAppFormComponent,
    OAuthAppModalComponent
  ],
  providers: []
})
export class SettingsModule {}
