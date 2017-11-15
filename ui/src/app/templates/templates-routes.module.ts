import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { TemplatesModule } from './templates.module';
import { TemplatesListPage } from './list-page/list-page.component';

const routes: Routes = [
  { path: '', component: TemplatesListPage, pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forChild(routes), TemplatesModule]
})
export class TemplateRoutesModule {}
