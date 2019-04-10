import {
  Component,
  OnInit,
  OnDestroy,
  OnChanges,
  Input,
  ViewChild,
  TemplateRef
} from '@angular/core';
import { ModalService } from '@syndesis/ui/common/modal/modal.service';

@Component({
  selector: 'syndesis-modal',
  templateUrl: './modal.component.html'
})
export class ModalComponent implements OnInit, OnDestroy, OnChanges {
  @Input() id = 'modal';
  @Input() title: string;
  @Input() lead: string;
  @Input() message: string;
  @Input() type: string;
  @Input() primaryText: string;
  @Input() body: TemplateRef<any>;
  @ViewChild('template') public template: TemplateRef<any>;

  constructor(private modalService: ModalService) {}

  ngOnInit(): void {
    this.modalService.registerModal(this.id, this.template);
  }

  ngOnDestroy(): void {
    this.modalService.unregisterModal(this.id);
  }

  ngOnChanges(changes: any): void {
    const idChange = changes['id'];
    if (idChange && idChange.previousValue) {
      this.modalService.unregisterModal(idChange.previousValue);
      this.modalService.registerModal(idChange.currentValue, this.template);
    }
  }

  ok(): void {
    this.modalService.hide(this.id, true);
  }

  cancel(): void {
    this.modalService.hide(this.id, false);
  }
}
