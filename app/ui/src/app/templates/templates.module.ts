import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { SyndesisCommonModule } from '../common/common.module';
import { TemplatesListPage } from './list-page/list-page.component';
import { ListToolbarComponent } from './list-toolbar/list-toolbar.component';
import { TemplatesListComponent } from './list/list.component';

@NgModule({
  imports: [CommonModule, FormsModule, RouterModule, SyndesisCommonModule],
  declarations: [
    TemplatesListPage,
    TemplatesListComponent,
    ListToolbarComponent
  ],
  exports: [TemplatesListComponent]
})
export class TemplatesModule {}
