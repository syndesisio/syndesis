import { Component, OnInit } from '@angular/core';
import { OAuthAppStore } from '@syndesis/ui/store/oauthApp/oauth-app.store';
import { OAuthApp, OAuthApps } from '@syndesis/ui/settings';
import { Observable } from 'rxjs';
import { ConfigService } from '@syndesis/ui/config.service';

import { ObjectPropertyFilterConfig } from '@syndesis/ui/common/object-property-filter.pipe';
import { ObjectPropertySortConfig } from '@syndesis/ui/common/object-property-sort.pipe';
import {
  FilterConfig,
  SortConfig,
  ToolbarConfig,
  CopyEvent,
  NotificationType
} from 'patternfly-ng';

import { NotificationService } from '@syndesis/ui/common';

export interface OAuthAppListItem {
  expanded: boolean;
  client: OAuthApp;
}

@Component({
  selector: 'syndesis-oauth-apps',
  templateUrl: 'oauth-apps.component.html',
  styleUrls: ['./oauth-apps.component.scss']
})
export class OAuthAppsComponent implements OnInit {
  // Holds the candidate for clearing credentials
  selectedItem: OAuthAppListItem;
  // Pipe configuration
  filter: ObjectPropertyFilterConfig = {
    filter: '',
    propertyName: 'client.name'
  };
  sort: ObjectPropertySortConfig = {
    sortField: 'client.name',
    descending: false
  };
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
  list: Observable<OAuthApps>;
  loading: Observable<boolean>;
  isLoading = true;

  items: Array<OAuthAppListItem> = [];

  callbackURL: string;

  constructor(
    public store: OAuthAppStore,
    public config: ConfigService,
    private notificationService: NotificationService
  ) {
    this.loading = store.loading;
    this.list = store.list;
  }

  // Handles events when the user interacts with the toolbar filter
  filterChanged($event) {
    // TODO update our pipe to handle multiple filters
    if ($event.appliedFilters.length === 0) {
      this.filter.filter = '';
    }
    $event.appliedFilters.forEach(filter => {
      this.filter.propertyName = filter.field.id;
      this.filter.filter = filter.value;
    });
  }

  // Handles events when the user interacts with the toolbar sort
  sortChanged($event) {
    this.sort.sortField = $event.field.id;
    this.sort.descending = !$event.isAscending;
  }

  // Returns whether or not this item has stored credentials
  isConfigured(item) {
    const client = item.client || {};
    const properties = client.properties;
    const configuredProperties = client.configuredProperties;
    if (!configuredProperties) {
      return false;
    }
    return Object.keys(properties).find(key => {
      const property = properties[key];
      if (!property.required) {
        return false;
      }
      const value = configuredProperties[key];
      return value === null || value === undefined || value === '';
    }) === undefined;
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

  // view initialization
  ngOnInit() {
    this.list.subscribe((apps: OAuthApps) => {
      const oldItems = this.items;
      this.items = [];
      for (const app of apps) {
        const oldApp = oldItems.find(item => {
          return item.client.id === app.id;
        });
        this.items.push({
          expanded: oldApp ? oldApp.expanded : false,
          client: app
        });
      }
    });
    this.store.loadAll();
    this.callbackURL =
      window.location.protocol +
      '//' +
      window.location.hostname +
      '/api/v1/credentials/callback';
  }

  handleCopy($event: CopyEvent, result: any): void {
    this.notify(result);
  }

  notify(result: any): void {
    this.notificationService.message(
      NotificationType.SUCCESS,
      null,
      result.msg,
      false,
      null,
      null);
  }
}
