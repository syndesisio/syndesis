import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { ActionConfig, ListConfig, EmptyStateConfig } from 'patternfly-ng';

import { log, getCategory } from '@syndesis/ui/logging';
import { ConfigService } from '@syndesis/ui/config.service';

import { ApiConnector, ApiConnectors } from '@syndesis/ui/customizations/api-connector';
import { ApiConnectorService } from './../api-connector.service';

@Component({
  selector: 'syndesis-api-connector-list',
  templateUrl: './api-connector-list.component.html',
  styleUrls: ['./api-connector-list.component.scss']
})
export class ApiConnectorListComponent implements OnInit {
  apiConnectors$: Observable<ApiConnectors|any>;
  filteredApiConnectors$ = new BehaviorSubject(<ApiConnectors>{});
  loading$ = Observable.of(true);
  listConfig: ListConfig;
  appName: string;
  itemUseMapping: { [valueComparator: string]: string } = {
    '=1': '<strong>1</strong> time',
    'other': '<strong>#</strong> times'
  };

  constructor(
    private apiConnectorService: ApiConnectorService,
    private config: ConfigService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.listConfig = {
      dblClick: false,
      multiSelect: false,
      selectItems: false,
      showCheckbox: false,
      emptyStateConfig: {
        iconStyleClass: 'pficon pficon-add-circle-o',
        title: 'Create API Connector',
        info:
          'There are currently no API connectors available. Please click on the button below to create one.',
        actions: {
          primaryActions: [
            {
              id: 'createApiConnector',
              title: 'Create API Connector',
              tooltip: 'Create API Connector'
            }
          ],
          moreActions: []
        } as ActionConfig
      } as EmptyStateConfig
    };
  }

  ngOnInit() {
    this.appName = this.config.getSettings('branding', 'appName', 'Syndesis');
    this.apiConnectors$ = this.apiConnectorService.list();
    this.loading$ = this.apiConnectors$.switchMap(apiConnectors => Observable.of(!apiConnectors));
  }

  handleAction(event: any) {
    if (event.id === 'createApiConnector') {
      this.router.navigate(['create'], { relativeTo: this.route });
    }
  }

  handleClick(event: { item: ApiConnector }) {
    const apiConnector = event.item;
    this.router.navigate([apiConnector.id], { relativeTo: this.route });
  }
}
