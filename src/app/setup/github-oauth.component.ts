import { Component, OnInit, ViewChild, ChangeDetectorRef } from '@angular/core';

import { Observable } from 'rxjs/Observable';
import { NotificationType } from 'patternfly-ng';

export interface OAuthAppListItem {
  expanded: boolean;
}

@Component({
  selector: 'syndesis-github-oauth',
  templateUrl: 'github-oauth.component.html',
  styleUrls: ['./github-oauth.component.scss'],
})
export class GitHubOAuthSetupComponent implements OnInit {

  header: string = 'Default Header.';
  message: string = 'Default Message.';
  dismissible: false;
  type: NotificationType;
  types: NotificationType[];
  hidden: string;
  actionText: string = '';
  loading: Observable<boolean>;

  constructor(public detector: ChangeDetectorRef) {}

  /**
   * Connects an account to GitHub via OAuth.
   */
  connectGitHub() {}

  /**
   * Disconnects a previously connected GitHub account
   */
  disconnectGitHub() {}

  /**
   * Returns whether or not this item has stored credentials
   */
  isConfigured(item) {
    const client = item.client || {};
    return (
      client.clientId &&
      client.clientId !== '' &&
      (client.clientSecret && client.clientSecret !== '')
    );
  }

  /**
   * View initialization
    */
  ngOnInit(): void {
    this.types = [
      NotificationType.SUCCESS,
      NotificationType.INFO,
      NotificationType.DANGER,
      NotificationType.WARNING,
    ];
    this.type = this.types[0];
  }

  handleType(item: string): void {
    this.type = item;
  }
}
