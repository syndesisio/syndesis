import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { EffectsModule } from '@ngrx/effects';

import { IntegrationsListPage } from './list-page/list-page.component';
import { IntegrationsListToolbarComponent } from './list-toolbar/list-toolbar.component';
import { IntegrationsFilterPipe } from './integrations-filter.pipe';
import { IntegrationsListComponent } from './list/list.component';
import { IntegrationsCreatePage } from './create-page/create-page.component';
import { IntegrationEffects } from '../store/integration/integration.effects';

const routes: Routes = [
  { path: '', component: IntegrationsListPage, pathMatch: 'full' },
  { path: 'create', component: IntegrationsCreatePage, pathMatch: 'full' },
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    RouterModule.forChild(routes),
    EffectsModule.run(IntegrationEffects),
  ],
  declarations: [
    IntegrationsListPage,
    IntegrationsListToolbarComponent,
    IntegrationsListComponent,
    IntegrationsFilterPipe,
    IntegrationsCreatePage,
  ],
})
export class IntegrationsModule { }
