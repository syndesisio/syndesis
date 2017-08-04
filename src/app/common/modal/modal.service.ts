import { Injectable, OnInit, ViewChild, TemplateRef } from '@angular/core';
import { BsModalService } from 'ngx-bootstrap/modal';
import { BsModalRef } from 'ngx-bootstrap/modal/modal-options.class';
import { Modal } from './modal';

@Injectable()
export class ModalService {

  private registeredModals = new Map<string, Modal>();

  constructor(private bsModalService: BsModalService) {}

  registerModal(id: string, template: TemplateRef<any>): void {
    this.registeredModals.set(id, {template: template});
  }

  unregisterModal(id: string): void {
    this.registeredModals.delete(id);
  }

  show(id: string = 'modal'): Promise<boolean> {
    return new Promise((resolve, reject) => {
      const modal = this.registeredModals.get(id);
      modal.bsModalRef = this.bsModalService.show(modal.template);
      const subscription = this.bsModalService.onHide.subscribe(event => {
        subscription.unsubscribe();
        const result = modal.result;
        resolve(result);
      });
    });
  }

  hide(id: string, result: boolean): void {
    const modal = this.registeredModals.get(id);
    modal.result = result;
    modal.bsModalRef.hide();
  }

}
