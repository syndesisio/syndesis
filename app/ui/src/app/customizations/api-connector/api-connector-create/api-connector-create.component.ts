import { Component, OnInit, ViewChild, TemplateRef, OnDestroy } from '@angular/core';

import { Router } from '@angular/router';
import { ModalService } from '@syndesis/ui/common';

@Component({
  selector: 'syndesis-api-connector-create',
  styleUrls: ['./api-connector-create.component.scss'],
  templateUrl: './api-connector-create.component.html'
})
export class ApiConnectorCreateComponent implements OnInit, OnDestroy {
  TEMP_STEP = 1;
  @ViewChild('cancelModalTemplate') cancelModalTemplate: TemplateRef<any>;

  private cancelModalId = 'create-cancellation-modal';

  constructor(
    private router: Router,
    private modalService: ModalService
  ) { }

  ngOnInit() {
    this.modalService.registerModal(this.cancelModalId, this.cancelModalTemplate);
  }

  showCancelModal(): void {
    this.modalService.show(this.cancelModalId).then(modal => {
      if (modal.result) {
        this.router.navigate(['customizations', 'api-connector']);
      }
    });
  }

  onCancel(doCancel: boolean): void {
    this.modalService.hide(this.cancelModalId, doCancel);
  }

  ngOnDestroy() {
    this.modalService.unregisterModal(this.cancelModalId);
  }

  onFetchComplete(): void {
    this.TEMP_STEP = 2;
  }

  onReviewComplete(): void {
    this.TEMP_STEP = 3;
  }

  onAuthSetup(): void {
    this.TEMP_STEP = 4;
  }

  onCreateComplete(event): void {
    console.log(event);
  }
}
