import { TemplateRef } from '@angular/core';
import { BsModalRef } from 'ngx-bootstrap/modal/bs-modal-ref.service';

export interface Modal {
  template: TemplateRef<any>;
  bsModalRef?: BsModalRef;
  result?: boolean;
}
