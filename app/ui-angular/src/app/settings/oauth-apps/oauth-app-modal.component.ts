import { take } from 'rxjs/operators';
import { Component } from '@angular/core';
import { NotificationType } from 'patternfly-ng';

import { OAuthAppListItem } from '@syndesis/ui/settings/oauth-apps/oauth-apps.component';
import { OAuthAppStore } from '@syndesis/ui/store/oauthApp/oauth-app.store';
import { ModalService } from '@syndesis/ui/common/modal/modal.service';
import { NotificationService } from '@syndesis/ui/common/ui-patternfly/notification-service';

@Component({
  selector: 'syndesis-oauth-app-modal',
  templateUrl: './oauth-app-modal.component.html'
})
export class OAuthAppModalComponent {
  // Holds the candidate for clearing credentials
  item: OAuthAppListItem;
  constructor(
    public store: OAuthAppStore,
    private modalService: ModalService,
    private notificationService: NotificationService
  ) {}

  show(item: OAuthAppListItem) {
    this.item = item;
    this.modalService.show().then(
      modal =>
        modal.result
          ? this.removeCredentials()
              .then(app => (this.item.client = app))
              .then(_ =>
                this.notificationService.popNotification({
                  type: NotificationType.SUCCESS,
                  header: 'Delete Successful',
                  message: 'Settings successfully deleted.'
                })
              )
              .catch(error =>
                this.notificationService.popNotification({
                  type: NotificationType.DANGER,
                  header: 'Delete Failed',
                  message: `Failed to delete settings: ${error}`
                })
              )
          : undefined
    );
  }

  // Clear the store credentials for the selected oauth app
  removeCredentials() {
    const app = {
      ...this.item.client,
    };
    delete app.configuredProperties;
    return this.store
      .delete(app)
      .pipe(take(1))
      .toPromise();
  }
}
