import { map, first, combineLatest, switchMap } from 'rxjs/operators';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Store, select } from '@ngrx/store';
import { Observable } from 'rxjs';

import {
  ApiConnectorActions,
  ApiConnectorData,
  ApiConnectors,
  ApiConnectorState,
  ApiConnectorStore,
  getApiConnectorState,
  CustomConnectorRequest,
  getApiConnectorRequest
} from '@syndesis/ui/customizations/api-connector';
import { ConfigService } from '@syndesis/ui/config.service';

@Component({
  selector: 'syndesis-api-connector-detail',
  templateUrl: 'api-connector-detail.component.html',
  styleUrls: ['api-connector-detail.component.scss']
})
export class ApiConnectorDetailComponent implements OnInit {
  readonly apiEndpoint: String;
  apiConnectorState$: Observable<ApiConnectorState>;
  apiConnectorData$: Observable<ApiConnectorData>;
  createRequest$: Observable<CustomConnectorRequest>;

  constructor(
    private apiConnectorStore: Store<ApiConnectorStore>,
    private route: ActivatedRoute,
    private config: ConfigService
  ) {
    this.apiEndpoint = this.config.getSettings().apiEndpoint;
  }

  ngOnInit() {
    this.apiConnectorState$ = this.apiConnectorStore.pipe(select(getApiConnectorState));
    this.createRequest$ = this.apiConnectorStore.pipe(select(getApiConnectorRequest));

    this.apiConnectorData$ = this.route.paramMap
      .pipe(
        first(params => params.has('id')),
        map(params => params.get('id')),
        // tslint:disable-next-line:deprecation
        combineLatest(
          this.apiConnectorState$.pipe(
            map(apiConnectorState => apiConnectorState.list)
          )
        ),
        switchMap(([id, apiConnectors]: [string, ApiConnectors]) =>
          apiConnectors.filter(apiConnector => apiConnector.id == id)
        )
      );
  }

  onUpdate(customConnectorRequest: CustomConnectorRequest): void {
    this.apiConnectorStore.dispatch(
      ApiConnectorActions.update(customConnectorRequest)
    );
  }
}
