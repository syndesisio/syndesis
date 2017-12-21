import { Component, Inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { ApiConnector, ApiConnectorData } from './api-connector.models';
import { ApiConnectorStore } from './api-connector.store';

import { Restangular } from 'ngx-restangular';


@Component({
  selector: 'syndesis-api-connector-detail',
  templateUrl: 'api-connector-detail.component.html',
  styleUrls: ['api-connector-detail.component.scss']
})
export class ApiConnectorDetailComponent implements OnInit {
  apiConnectorData$: Observable<ApiConnectorData>;
  loading$: Observable<boolean>;

  constructor(private apiConnectorStore: ApiConnectorStore,
              private router: Router,
              private route: ActivatedRoute,
              @Inject(Restangular) public Restangular) {

    //this.loading$ = this.apiConnectorStore.loading;
    //this.apiConnectorData$ = this.apiConnectorStore.resource;
  }

  ngOnInit() {
    this.route.paramMap
      .first(params => params.has('id'))
      .map(params => params.get('id'))
      //.subscribe(id => this.apiConnectorStore.load(id));
      .subscribe(id => this.Restangular.one('connectors/').get(id));
  }
}
