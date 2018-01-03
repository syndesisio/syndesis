import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Store } from '@ngrx/store';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { ActionConfig, ListConfig, EmptyStateConfig } from 'patternfly-ng';

import { log, getCategory } from '@syndesis/ui/logging';
import { ConfigService } from '@syndesis/ui/config.service';
import {
  ApiConnectorData, ApiConnectors, ApiConnectorState,
  ApiConnectorStore, getApiConnectorState
} from '@syndesis/ui/customizations/api-connector';

@Component({
  selector: 'syndesis-api-connector-list',
  templateUrl: './api-connector-list.component.html',
  styleUrls: ['./api-connector-list.component.scss']
})
export class ApiConnectorListComponent implements OnInit {
  apiConnectorState$: Observable<ApiConnectorState>;
  filteredApiConnectors$ = new BehaviorSubject(<ApiConnectors>{});
  listConfig: ListConfig;
  appName: string;
  itemUseMapping: { [valueComparator: string]: string } = {
    '=1': '<strong>1</strong> time',
    'other': '<strong>#</strong> times'
  };

  get apiConnectors$(): Observable<ApiConnectors> {
    return this.apiConnectorState$.map(apiConnectorState => apiConnectorState.list);
  }

  constructor(
    private apiConnectorStore: Store<ApiConnectorStore>,
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
    this.apiConnectorState$ = this.apiConnectorStore.select<ApiConnectorState>(getApiConnectorState);
  }

  handleAction(event: any) {
    if (event.id === 'createApiConnector') {
      this.router.navigate(['create'], { relativeTo: this.route });
    }
  }

  handleClick(event: { item: ApiConnectorData }) {
    const apiConnector = event.item;
    this.router.navigate([apiConnector.id], { relativeTo: this.route });
  }
}
