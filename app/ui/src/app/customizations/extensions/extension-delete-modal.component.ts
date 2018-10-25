import {
  Component,
  OnInit,
  OnDestroy,
  TemplateRef,
  ViewChild
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationType } from 'patternfly-ng';
import { Extension, I18NService, Integrations } from '@syndesis/ui/platform';
import { ExtensionStore } from '@syndesis/ui/store/extension/extension.store';
import { ModalService } from '@syndesis/ui/common/modal/modal.service';
import { NotificationService } from '@syndesis/ui/common/ui-patternfly/notification-service';

@Component({
  selector: 'syndesis-extension-delete-modal',
  templateUrl: 'extension-delete-modal.component.html',
  styleUrls: [
    'extension-common.scss',
    'extension-delete-modal.component.scss'
  ]
})
export class ExtensionDeleteModalComponent implements OnInit, OnDestroy {
  id = 'extension-delete-modal';
  extension: Extension;
  integrations: Integrations;
  loading = true;
  @ViewChild('template') public template: TemplateRef<any>;

  constructor(
    private extensionStore: ExtensionStore,
    private modalService: ModalService,
    private notificationService: NotificationService,
    private router: Router,
    private route: ActivatedRoute,
    private i18NService: I18NService
  ) {}

  public prompt(item: Extension) {
    this.extension = item;
    this.extensionStore.loadIntegrations(this.extension.id).subscribe(
      integrations => {
        this.loading = false;
        this.integrations = integrations;
      },
      err => {
        this.loading = false;
      }
    );
    this.modalService.show(this.id).then(modal => {
      if (modal.result) {
        this.extensionStore.delete(this.extension).subscribe(
          ext => {
            this.notificationService.popNotification({
              type: NotificationType.SUCCESS,
              header: this.i18NService.localize( 'customizations.extensions.delete-extension-modal-success-header' ),
              message: this.i18NService.localize( 'customizations.extensions.delete-extension-modal-success-message',
                                                  [ this.extension.name ] )
            });
            if ('id' in this.route.snapshot.params) {
              this.router.navigate(['..'], { relativeTo: this.route });
            }
          },
          err => {
            this.notificationService.popNotification({
              type: NotificationType.WARNING,
              header: this.i18NService.localize( 'customizations.extensions.delete-extension-modal-error-header' ),
              message: this.i18NService.localize(
                'customizations.extensions.delete-extension-modal-error-message',
                [ this.extension.name,
                        err.userMsg
                        || this.i18NService.localize( 'customizations.extensions.import-extension-unknown-error-message' ) ] )
            });
          }
        );
      } else {
        this.extension = undefined;
      }
    });
  }

  delete(): void {
    this.modalService.hide(this.id, true);
  }

  cancel(): void {
    this.modalService.hide(this.id, false);
  }

  ngOnInit(): void {
    this.modalService.registerModal(this.id, this.template);
  }

  ngOnDestroy(): void {
    this.modalService.unregisterModal(this.id);
  }
}
