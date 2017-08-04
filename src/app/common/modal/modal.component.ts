import { Component, OnInit, OnDestroy, Input, Output, ViewChild, TemplateRef } from '@angular/core';
import { ModalService } from './modal.service';

@Component({
  selector: 'syndesis-modal',
  templateUrl: './modal.component.html',
})
export class ModalComponent implements OnInit, OnDestroy {

  @Input() id = 'modal';
  @Input() title: string;
  @Input() message: string;
  @ViewChild('template') public template: TemplateRef<any>;

  constructor(private modalService: ModalService) {}

  ngOnInit(): void {
    this.modalService.registerModal(this.id, this.template);
  }

  ngOnDestroy(): void {
    this.modalService.unregisterModal(this.id);
  }

  ok(): void {
    this.modalService.hide(this.id, true);
  }

  cancel(): void {
    this.modalService.hide(this.id, false);
  }

}
