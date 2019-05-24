import { Component, OnInit, OnDestroy } from '@angular/core';
import { OAuthAppStore } from '@syndesis/ui/store/oauthApp/oauth-app.store';
import { OAuthApp, OAuthApps } from '@syndesis/ui/settings';
import { Observable, BehaviorSubject, Subscription } from 'rxjs';
import { ConfigService } from '@syndesis/ui/config.service';

import {
  FilterConfig,
  SortConfig,
  ToolbarConfig,
  FilterField
} from 'patternfly-ng';

export interface OAuthAppListItem {
  name: string;
  expanded: boolean;
  client: OAuthApp;
  message?: string;
  error?: any;
}

@Component({
  selector: 'syndesis-oauth-apps',
  templateUrl: 'oauth-apps.component.html',
  styleUrls: ['./oauth-apps.component.scss']
})
export class OAuthAppsComponent implements OnInit, OnDestroy {
  // Holds the candidate for clearing credentials
  selectedItem: OAuthAppListItem;
  // List configuration
  listConfig = {
    multiSelect: false,
    selectItems: false,
    showCheckbox: false
  };
  // Toolbar configuration
  toolbarConfig = {
    filterConfig: {
      fields: [
        {
          id: 'client.name',
          title: 'Name',
          placeholder: 'Filter by Name...',
          type: 'text'
        }
      ]
    } as FilterConfig,
    sortConfig: {
      fields: [
        {
          id: 'client.name',
          title: 'Name',
          sortType: 'alpha'
        }
      ],
      isAscending: true
    } as SortConfig
  } as ToolbarConfig;
  // Data
  loading$: Observable<boolean>;
  oauthApps$ = new BehaviorSubject<Array<OAuthAppListItem>>([]);
  filteredOAuthApps$ = new BehaviorSubject<Array<OAuthAppListItem>>([]);
  isLoading = true;

  items: Array<OAuthAppListItem> = [];

  callbackURL: string;
  filterFields: Array<FilterField> = [];
  subscription: Subscription;

  constructor(
    public oauthAppStore: OAuthAppStore,
    public configService: ConfigService
  ) {
    this.loading$ = oauthAppStore.loading;
  }

  // Returns whether or not this item has stored credentials
  isConfigured(item) {
    const client = item.client || {};
    const properties = client.properties;
    const configuredProperties = client.configuredProperties;
    if (!configuredProperties) {
      return false;
    }
    return (
      Object.keys(properties).find(key => {
        const property = properties[key];
        if (!property.required || property.type === 'hidden') {
          return false;
        }
        const value = configuredProperties[key];
        return value === null || value === undefined || value === '';
      }) === undefined
    );
  }

  handleLinks(event: any): void {
    event.stopPropagation();
    event.preventDefault();

    if (
      event.target &&
      event.target.tagName &&
      event.target.tagName.toLowerCase() === 'a'
    ) {
      window.open(event.target.getAttribute('href'), '_blank');
    }
  }

  onSave() {
    this.oauthAppStore.loadAll();
  }

  // view initialization
  ngOnInit() {
    this.subscription = this.oauthAppStore.list.subscribe((apps: OAuthApps) => {
      const oldItems = this.items;
      this.items = [];
      for (const app of apps) {
        const oldApp = oldItems.find(item => {
          return item.client.id === app.id;
        });
        this.items.push({
          name: app.name,
          message: oldApp ? oldApp.message : undefined,
          error: oldApp ? oldApp.error : undefined,
          expanded: oldApp ? oldApp.expanded : false,
          client: app
        });
      }
      this.oauthApps$.next(this.items);
    });
    this.oauthAppStore.loadAll();
    this.callbackURL =
      window.location.protocol +
      '//' +
      window.location.hostname +
      '/api/v1/credentials/callback';
  }

  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}
