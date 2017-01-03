import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { TemplatesListPage } from './list-page/list-page.component';
import { ListToolbarComponent } from './list-toolbar/list-toolbar.component';

const routes: Routes = [
  { path: '', component: TemplatesListPage, pathMatch: 'full' },
];

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    RouterModule.forChild(routes),
  ],
  declarations: [
    TemplatesListPage,
    ListToolbarComponent,
  ],
})
export class TemplatesModule { }
