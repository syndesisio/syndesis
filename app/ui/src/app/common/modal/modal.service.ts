import { take } from 'rxjs/operators';
import { Injectable, TemplateRef } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap';
import { Modal } from '@syndesis/ui/common/modal/modal.models';

@Injectable()
export class ModalService {
  private registeredModals = new Map<string, Modal>();

  constructor(private bsModalService: BsModalService) {}

  registerModal(id: string, template: TemplateRef<any>): void {
    this.registeredModals.set(id, { template: template });
  }

  unregisterModal(id: string): void {
    this.registeredModals.delete(id);
  }

  show(id = 'modal'): Promise<Modal> {
    const modal = this.registeredModals.get(id);
    modal.bsModalRef = this.bsModalService.show(modal.template, {
      ignoreBackdropClick: true,
      class: 'message-dialog-pf'
    });
    return this.bsModalService.onHide
      .pipe(take(1))
      .toPromise()
      .then(_ => modal);
  }

  hide(id: string, result: boolean): void {
    const modal = this.registeredModals.get(id);
    modal.result = result;
    modal.bsModalRef.hide();
  }
}
