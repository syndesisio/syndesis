import { Component, OnInit, ViewChild, ChangeDetectorRef, ViewEncapsulation } from '@angular/core';
import { Http } from '@angular/http';
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

  stepOneComplete = false;
  stepTwoComplete = false;
  stepThreeComplete = false;
  loading: Observable<boolean>;
  noAccountConnected = true;
  validCredentials: true;

  /**
   * @param {ChangeDetectorRef} detector
   */
  constructor(public detector: ChangeDetectorRef) {}

  /**
   * Step Two
   * Validation - Checks if provided credentials are valid.
   */
  checkCredentials($event) {
    if($event) {
      console.log('$event: ' + JSON.stringify($event));
      this.stepTwoComplete = true;
    }
  }

  /**
   * Step Two
   */
  connectGitHub(http: Http) {
    //http.get('https://api.github.com').map(res => res.json()).subscribe(accounts => this.accounts = accounts);
  }

  /**
   * Disconnects a previously connected GitHub account
   */
  disconnectGitHub() {}

  /**
   * All steps have been completed
   */
  getStarted() {}

  /**
   * Step One
   * Registers Syndesis as an OAuth application on GitHub
   */
  registerSyndesis() {
    this.stepOneComplete = true;
    window.open('https://github.com/settings/applications/new', '_blank');
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
