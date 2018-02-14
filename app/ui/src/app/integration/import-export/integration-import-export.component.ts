import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ModalService } from '@syndesis/ui/common';

@Component({
  selector: 'syndesis-integration-import-export',
  templateUrl: 'integration-import-export.component.html'
})
export class IntegrationImportExportComponent implements OnInit {
  @ViewChild('cancelModalTemplate') cancelModalTemplate: TemplateRef<any>;

  private cancelModalId = 'create-cancellation-modal';

  constructor(private route: ActivatedRoute,
              private router: Router,
              private modalService: ModalService) {}

  ngOnInit() {
    this.modalService.registerModal(this.cancelModalId, this.cancelModalTemplate);
  }

  onCancel(doCancel: boolean): void {
    this.modalService.hide(this.cancelModalId, doCancel);
  }

  showCancelModal(): void {
    this.modalService.show(this.cancelModalId).then(modal => {
      if (modal.result) {
        this.redirectBack();
      }
    });
  }

  private redirectBack(): void {
    this.router.navigate(['/integrations']);
  }

  ngOnDestroy() {
    this.modalService.unregisterModal(this.cancelModalId);
  }
}
