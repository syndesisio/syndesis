import { TemplateRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/modal-options.class';

export interface Modal {
  template: TemplateRef<any>;
  bsModalRef?: BsModalRef;
  result?: boolean;
}
