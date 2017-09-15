import { Component, OnInit, ViewChild, ChangeDetectorRef } from '@angular/core';

/**
 * The GitHub account model
 */
//import { GitHubAccount } from '../model';
import { Observable } from 'rxjs/Observable';

export interface OAuthAppListItem {
  expanded: boolean;
  //account: GitHubAccount;
}

@Component({
  selector: 'syndesis-github-oauth',
  templateUrl: 'github-oauth.component.html',
  styleUrls: ['./github-oauth.component.scss'],
})
export class GitHubOAuthSetupComponent implements OnInit {

  loading: Observable<boolean>;
  isLoading = true;

  constructor(public detector: ChangeDetectorRef) {}

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
  ngOnInit() {}
}
