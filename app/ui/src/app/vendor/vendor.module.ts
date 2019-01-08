import { NgModule } from '@angular/core';

import { FileUploadModule } from 'ng2-file-upload';

import {
  AlertModule,
  BsDropdownModule,
  BsModalRef,
  CollapseModule,
  ComponentLoaderFactory,
  ModalModule,
  PopoverModule,
  TooltipModule,
  TypeaheadModule,
} from 'ngx-bootstrap';

import {
  ActionModule,
  BlockCopyModule,
  CardModule,
  InlineCopyModule,
  InlineNotificationModule,
  ListModule,
  PaginationModule,
  ToastNotificationListModule,
  ToastNotificationModule,
  ToolbarModule,
} from 'patternfly-ng';

import { CodemirrorModule } from 'ng2-codemirror';

const imports = [
  ActionModule,
  AlertModule.forRoot(),
  BlockCopyModule,
  BsDropdownModule.forRoot(),
  CardModule,
  CodemirrorModule,
  CollapseModule.forRoot(),
  FileUploadModule,
  InlineCopyModule,
  InlineNotificationModule,
  ListModule,
  ModalModule.forRoot(),
  PopoverModule.forRoot(),
  ToastNotificationListModule,
  ToastNotificationModule,
  ToolbarModule,
  TooltipModule.forRoot(),
  TypeaheadModule.forRoot(),
];

const _exports = [
  ActionModule,
  AlertModule,
  BlockCopyModule,
  BsDropdownModule,
  CardModule,
  CodemirrorModule,
  CollapseModule,
  FileUploadModule,
  InlineCopyModule,
  InlineNotificationModule,
  ListModule,
  ModalModule,
  PaginationModule,
  PopoverModule,
  ToastNotificationListModule,
  ToastNotificationModule,
  ToolbarModule,
  TooltipModule,
  TypeaheadModule,
];

@NgModule({
  imports: imports,
  providers: [ComponentLoaderFactory, BsModalRef],
  exports: _exports
})
export class VendorModule {}
