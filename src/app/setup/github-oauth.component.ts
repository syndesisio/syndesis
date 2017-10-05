import { Component, OnInit, ViewChild, ChangeDetectorRef, ViewEncapsulation } from '@angular/core';

import { Observable } from 'rxjs/Observable';

export interface OAuthAppListItem {
  expanded: boolean;
}

@Component({
  selector: 'syndesis-github-oauth',
  templateUrl: 'github-oauth.component.html',
  styleUrls: ['./github-oauth.component.scss'],
})
export class GitHubOAuthSetupComponent implements OnInit {

  hidden = true;
  stepOneComplete = false;
  stepTwoComplete = false;
  stepThreeComplete = false;
  loading: Observable<boolean>;
  noAccountConnected = true;

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
   * Registers Syndesis as an OAuth application on GitHub
   */
  registerSyndesis() {}

  /**
   * User has selected an account
   */
  selectedAccount() {}

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
    /**
     * Loads the possible GitHub accounts
     */
  }
}
