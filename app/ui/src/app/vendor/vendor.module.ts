import { NgModule } from '@angular/core';

import {
  AlertModule,
  CollapseModule,
  ModalModule,
  PopoverModule,
  TabsModule,
  TooltipModule,
  TypeaheadModule,
  BsDropdownModule,
  ComponentLoaderFactory
} from 'ngx-bootstrap';

import {
  ActionModule,
  NotificationModule,
  CardModule,
  ListModule,
  ToolbarModule,
  PaginationModule,
} from 'patternfly-ng';

const imports = [
  AlertModule.forRoot(),
  CollapseModule.forRoot(),
  ModalModule.forRoot(),
  PopoverModule.forRoot(),
  TabsModule.forRoot(),
  TooltipModule.forRoot(),
  TypeaheadModule.forRoot(),
  BsDropdownModule.forRoot(),
  ActionModule,
  NotificationModule,
  CardModule,
  ListModule,
  ToolbarModule
];

const _exports = [
  AlertModule,
  CollapseModule,
  ModalModule,
  PopoverModule,
  TabsModule,
  TooltipModule,
  TypeaheadModule,
  BsDropdownModule,
  ActionModule,
  NotificationModule,
  CardModule,
  ListModule,
  ToolbarModule,
  PaginationModule
];

@NgModule({
  imports: imports,
  providers: [ComponentLoaderFactory],
  exports: _exports
})
export class VendorModule {}
