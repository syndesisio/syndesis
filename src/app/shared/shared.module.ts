import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { TemplatesListComponent } from '../templates/list/list.component';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
  ],
  declarations: [
    TemplatesListComponent,
  ],
  exports: [
    TemplatesListComponent,
  ],
})
export class SharedModule { }
