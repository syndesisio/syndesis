import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BsDropdownModule } from 'ngx-bootstrap';
import { IntegrationActionsComponent } from './actions.component';

@NgModule({
  imports: [
    CommonModule,
    BsDropdownModule,
  ],
  declarations: [
    IntegrationActionsComponent
  ],
  exports: [
    IntegrationActionsComponent
  ]
})
export class IntegrationActionsModule {
}
