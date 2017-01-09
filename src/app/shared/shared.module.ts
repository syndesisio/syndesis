import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { TemplatesListComponent } from '../templates/list/list.component';

@NgModule({
  imports: [
    CommonModule,
    RouterModule,
  ],
  declarations: [
    TemplatesListComponent,
  ],
  exports: [
    TemplatesListComponent,
  ],
})
export class SharedModule { }
