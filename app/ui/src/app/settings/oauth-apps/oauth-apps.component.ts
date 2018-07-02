import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { OAuthAppStore } from '../../store/oauthApp/oauth-app.store';
import { OAuthApp, OAuthApps } from '@syndesis/ui/settings';
import { Observable } from 'rxjs';
import { ConfigService } from '../../config.service';
import { UserService } from '@syndesis/ui/platform';

import { ObjectPropertyFilterConfig } from '../../common/object-property-filter.pipe';
import { ObjectPropertySortConfig } from '../../common/object-property-sort.pipe';

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
    },
    sortConfig: {
      fields: [
        {
          id: 'client.name',
          title: 'Name',
          sortType: 'alpha'
        }
      ],
      isAscending: true
    }
  };
  // Data
  list: Observable<OAuthApps>;
  loading: Observable<boolean>;
  isLoading = true;

  items: Array<OAuthAppListItem> = [];

  callbackURL: string;

  constructor(
    public store: OAuthAppStore,
    public config: ConfigService,
    private router: Router
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
    return (
      client.clientId &&
      client.clientId !== '' &&
      (client.clientSecret && client.clientSecret !== '')
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
}
