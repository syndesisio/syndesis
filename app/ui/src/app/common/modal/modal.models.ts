import { TemplateRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal';

export interface Modal {
  template: TemplateRef<any>;
  bsModalRef?: BsModalRef;
  result?: boolean;
}
