import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';

import { ConfigService } from '../../config.service';
import { ConnectorStore } from '../../store/connector/connector.store';
import { log, getCategory } from '../../logging';
import { Connectors } from '../../model';

import {
  Action,
  ActionConfig,
  ListConfig,
  ListEvent,
  EmptyStateConfig,
} from 'patternfly-ng';

const category = getCategory('ApiConnectors');

@Component({
  selector: 'syndesis-api-connector-list',
  templateUrl: './api-connector-list.component.html',
  styleUrls: ['./api-connector-list.component.scss']
})
export class ApiConnectorListComponent implements OnInit {
  connectors: Observable<Connectors>;
  filteredConnectors: Subject<Connectors> = new BehaviorSubject(<Connectors>{});
  loading: Observable<boolean>;
  listConfig: ListConfig;

  constructor(
    public config: ConfigService,
    private store: ConnectorStore,
  ) {
    this.loading = this.store.loading;
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
              id: 'importApiConnector',
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
    this.store.loadAll();
  }
}
