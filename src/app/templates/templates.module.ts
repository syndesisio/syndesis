import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { TemplatesListPage } from './list-page/list-page.component';
import { ListToolbarComponent } from './list-toolbar/list-toolbar.component';
import { TemplatesListComponent } from './list/list.component';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
  ],
  declarations: [
    TemplatesListPage,
    TemplatesListComponent,
    ListToolbarComponent,
  ],
  exports: [
    TemplatesListComponent,
  ]
})
export class TemplatesModule { }
