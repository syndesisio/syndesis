import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs/Observable';

import {
  ApiConnectorData, ApiConnectors, ApiConnectorState,
  ApiConnectorStore, getApiConnectorState
} from '@syndesis/ui/customizations/api-connector';

@Component({
  selector: 'syndesis-api-connector-detail',
  templateUrl: 'api-connector-detail.component.html',
  styleUrls: ['api-connector-detail.component.scss']
})
export class ApiConnectorDetailComponent implements OnInit {
  apiConnectorState$: Observable<ApiConnectorState>;
  apiConnectorData$: Observable<ApiConnectorData>;

  constructor(
    private apiConnectorStore: Store<ApiConnectorStore>,
    private route: ActivatedRoute,
  ) { }

  ngOnInit() {
    this.apiConnectorState$ = this.apiConnectorStore.select<ApiConnectorState>(getApiConnectorState);

    this.apiConnectorData$ = this.route.paramMap
      .first(params => params.has('id'))
      .map(params => params.get('id'))
      .combineLatest(this.apiConnectorState$.map(apiConnectorState => apiConnectorState.list))
      .switchMap(([id, apiConnectors]: [string, ApiConnectors]) => apiConnectors.filter(apiConnector => apiConnector.id == id));
  }
}
