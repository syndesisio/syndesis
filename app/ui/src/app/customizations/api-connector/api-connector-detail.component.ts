import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { ApiConnectorData } from './api-connector.models';
import { ApiConnectorStore } from './api-connector.store';
import { ApiConnectorService } from '@syndesis/ui/customizations/api-connector/api-connector.service';


@Component({
  selector: 'syndesis-api-connector-detail',
  templateUrl: 'api-connector-detail.component.html',
  styleUrls: ['api-connector-detail.component.scss']
})
export class ApiConnectorDetailComponent implements OnInit {
  apiConnectorData$: Observable<ApiConnectorData>;
  loading$: Observable<boolean>;

  constructor(private apiConnectorStore: ApiConnectorStore,
              private apiConnectorService: ApiConnectorService,
              private route: ActivatedRoute) {
    //this.loading$ = this.apiConnectorStore.loading;
    //this.apiConnectorData$ = this.apiConnectorStore.resource;
  }

  ngOnInit() {
    this.route.paramMap
      .first(params => params.has('id'))
      .map(params => params.get('id'))
      .subscribe(id => this.apiConnectorService.getApiConnector(id));
  }
}
