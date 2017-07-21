import { Component, OnInit } from '@angular/core';
import { ListToolbarProperties } from '../../common/toolbar/list-toolbar.component';

@Component({
  selector: 'syndesis-oauth-clients',
  templateUrl: 'oauth-clients.component.html',
})
export class OAuthClientsComponent implements OnInit {
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
      clientId: '',
      clientSecret: '',
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
  toolbarProperties = [
    {
      key: 'name',
      label: 'Name',
    },
  ];
  filter: undefined;
  sort: undefined;
  constructor() {}

  ngOnInit() {}
}
