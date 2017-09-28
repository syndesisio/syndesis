import { Component, OnInit, ViewChild, ChangeDetectorRef, ViewEncapsulation } from '@angular/core';
import { HttpModule } from '@angular/http';
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

  accounts = [];
  hidden = true;
  stepOneComplete = false;
  stepTwoComplete = false;
  stepThreeComplete = false;
  loading: Observable<boolean>;
  noAccountConnected = true;

  /**
   * @param {ChangeDetectorRef} detector
   * Use Http instead of Restangular, at least for now.
   */
  constructor(public detector: ChangeDetectorRef/*, http: Http*/) {}

  /**
   * Step Two
   * Connects an account to GitHub via OAuth.
   */
  connectGitHub() {
    this.stepTwoComplete = true;
  }

  /**
   * Disconnects a previously connected GitHub account
   */
  disconnectGitHub() {}

  /**
   * Fetch possible GitHub accounts
   */
  fetchAccounts(): void {
    /**
     * Load the possible GitHub accounts
     */
    /*
    // Replace with GH API endpoint
    http.get('gh-api-accounts-url').map(res => res.json()).subscribe(accounts => this.accounts = accounts);
    */
  }

  /**
   * All steps have been completed
   */
  getStarted() {}

  /**
   * Step One
   * Registers Syndesis as an OAuth application on GitHub
   */
  registerSyndesis() {
    window.open('https://github.com/settings/applications/new', '_blank');
    this.stepOneComplete = true;
  }

  /**
   * Step Three
   * User has selected an account
   */
  selectedAccount() {
    this.stepThreeComplete = true;
  }

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
  ngOnInit(): void {}
}
