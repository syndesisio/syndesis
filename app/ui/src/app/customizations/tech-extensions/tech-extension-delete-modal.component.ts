import {
  Component,
  OnInit,
  OnDestroy,
  TemplateRef,
  ViewChild
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NotificationType } from 'patternfly-ng';
import { Extension, Integrations } from '@syndesis/ui/platform';
import { ExtensionStore } from '@syndesis/ui/store/extension/extension.store';
import { ModalService } from '@syndesis/ui/common/modal/modal.service';
import { NotificationService } from '@syndesis/ui/common/ui-patternfly/notification-service';

@Component({
  selector: 'syndesis-tech-extension-delete-modal',
  templateUrl: 'tech-extension-delete-modal.component.html',
  styleUrls: [
    'tech-extension-common.scss',
    'tech-extension-delete-modal.component.scss'
  ]
})
export class TechExtensionDeleteModalComponent implements OnInit, OnDestroy {
  id = 'tech-extension-delete-modal';
  extension: Extension;
  integrations: Integrations;
  loading = true;
  @ViewChild('template') public template: TemplateRef<any>;

  constructor(
    private extensionStore: ExtensionStore,
    private modalService: ModalService,
    private notificationService: NotificationService,
    private router: Router,
    private route: ActivatedRoute
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
              header: 'Deleted!',
              message: `The extension "${this.extension.name}" has been deleted`
            });
            if ('id' in this.route.snapshot.params) {
              this.router.navigate(['..'], { relativeTo: this.route });
            }
          },
          err => {
            this.notificationService.popNotification({
              type: NotificationType.WARNING,
              header: 'Deleted!',
              message: `The extension "${
                this.extension.name
              }" could not be deleted due to: ${err.userMsg || 'Unknown error'}`
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
