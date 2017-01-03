import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { EffectsModule } from '@ngrx/effects';

import { DashboardComponent } from './dashboard.component';
import { EmptyStateComponent } from './emptystate.component';
import { PopularTemplatesComponent } from './populartemplates.component';
import { TemplateEffects } from '../store/template/template.effects';

const routes: Routes = [
  { path: '', component: DashboardComponent, pathMatch: 'full' },
];

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(routes),
    EffectsModule.run(TemplateEffects),
  ],
  declarations: [
    DashboardComponent,
    EmptyStateComponent,
    PopularTemplatesComponent,
  ],
})
export class DashboardModule { }
