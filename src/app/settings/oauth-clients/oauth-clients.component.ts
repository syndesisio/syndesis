import { Component, OnInit } from '@angular/core';
import { ObjectPropertyFilterConfig } from '../../common/object-property-filter.pipe';
import { ObjectPropertySortConfig } from '../../common/object-property-sort.pipe';

@Component({
  selector: 'syndesis-oauth-clients',
  templateUrl: 'oauth-clients.component.html',
})
export class OAuthClientsComponent implements OnInit {
  filter: ObjectPropertyFilterConfig = {
    filter: '',
    propertyName: 'client.name',
  };
  sort: ObjectPropertySortConfig = {
    sortField: 'client.name',
    descending: false,
  };
  listConfig = {
    multiSelect: false,
    selectItems: false,
    showCheckbox: false,
  };
  toolbarConfig = {
    filterConfig: {
      fields: [{
        id: 'client.name',
        title: 'Name',
        placeholder: 'Filter by Name...',
        type: 'text',
      }],
    },
    sortConfig: {
      fields: [{
        id: 'client.name',
        title: 'Name',
        sortType: 'alpha',
      }],
      isAscending: true,
    },
  };
  items = [];
  oauthClients = [
    {
      id: 'twitter',
      name: 'Twitter',
      icon: 'fa-twitter',
      clientId: '',
      clientSecret: '',
    },
    {
      id: 'saleforce',
      name: 'Salesforce',
      icon: 'fa-salesforce',
      clientId: 'blah',
      clientSecret: 'blah',
    },
    {
      id: 'facebook',
      name: 'Facebook',
      icon: 'fa-facebook',
      clientId: '',
      clientSecret: '',
    },
    {
      id: 'servicenow',
      name: 'ServiceNow',
      icon: 'fa-servicenow',
      clientId: '',
      clientSecret: '',
    },
  ];
  constructor() {}
  filterChanged($event) {
    // TODO update our pipe to handle multiple filters
    if ($event.appliedFilters.length === 0) {
      this.filter.filter = '';
    }
    $event.appliedFilters.forEach((filter) => {
      this.filter.propertyName = filter.field.id;
      this.filter.filter = filter.value;
    });
  }
  sortChanged($event) {
    this.sort.sortField = $event.field.id;
    this.sort.descending = !$event.isAscending;
  }
  isConfigured(item) {
    const client = item.client;
    return (client.clientId && client.clientId !== '') && (client.clientSecret && client.clientSecret !== '');
  }
  promptRemoveCredentials(item) {

  }

  ngOnInit() {
    this.oauthClients.forEach((client) => {
      this.items.push({
        expanded: false,
        client: client,
      });
    });
  }
}
