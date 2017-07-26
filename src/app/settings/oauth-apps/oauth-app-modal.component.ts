import { Component, OnInit, ViewChild, ChangeDetectorRef } from '@angular/core';
import { ModalDirective } from 'ngx-bootstrap';
import { OAuthAppListItem } from './oauth-apps.component';
import { OAuthAppStore } from '../../store/oauthApp/oauth-app.store';
import { OAuthApp, OAuthApps } from '../../model';
@Component({
  selector: 'syndesis-oauth-app-modal',
  templateUrl: './oauth-app-modal.component.html',
})
export class OAuthAppModal implements OnInit {
  // Modal
  @ViewChild('childModal') public childModal: ModalDirective;
  // Holds the candidate for clearing credentials
  item: OAuthAppListItem;
  constructor(
    public store: OAuthAppStore,
    public detector: ChangeDetectorRef,
  ) {}

  show(item: OAuthAppListItem) {
    this.item = item;
    this.childModal.show();
  }

  // Clear the store credentials for the selected oauth app
  removeCredentials() {
    if (!this.item) {
      // Shouldn't happen, but recover in some way
      this.hideModal();
      return;
    }
    const app = this.item.client;
    if (!app) {
      // Also shouldn't happen, but just in case
      this.hideModal();
      return;
    }
    app['clientId'] = '';
    app['clientSecret'] = '';
    this.hideModal();
    const sub = this.store.update(app).subscribe(
      resp => {
        // TODO toast notification
        sub.unsubscribe();
        if (this.item) {
          this.item.client = app;
        }
        this.detector.detectChanges();
      },
      error => {
        // TODO toast notification
        sub.unsubscribe();
        this.detector.detectChanges();
      },
    );
  }
  // Hides the confirmation dialog
  hideModal() {
    this.item = undefined;
    this.childModal.hide();
  }

  ngOnInit() {}
}
