import { Component, ChangeDetectorRef } from '@angular/core';
import { OAuthAppListItem } from './oauth-apps.component';
import { OAuthAppStore } from '../../store/oauthApp/oauth-app.store';
import { OAuthApp, OAuthApps } from '../../model';
import { ModalService } from '../../common/modal/modal.service';

@Component({
  selector: 'syndesis-oauth-app-modal',
  templateUrl: './oauth-app-modal.component.html',
})
export class OAuthAppModalComponent {

  // Holds the candidate for clearing credentials
  item: OAuthAppListItem;
  constructor(
    public store: OAuthAppStore,
    public detector: ChangeDetectorRef,
    private modalService: ModalService,
  ) {}

  show(item: OAuthAppListItem) {
    this.item = item;
    this.modalService.show()
      .then(result => result
        ? this.removeCredentials()
          // TODO toast notification
          .then(app => this.item.client = app)
          .catch(error => {})
          .then(_ => this.detector.markForCheck())
        : undefined);
  }

  // Clear the store credentials for the selected oauth app
  removeCredentials() {
    const app = { ...this.item.client, clientId: '', clientSecret: '' };
    return this.store.update(app).take(1).toPromise();
  }
}
