import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';

import { ApiConnector } from './api-connector.model';
import { ApiConnectorStore } from './api-connector.store';

@Component({
  selector: 'syndesis-api-connector-detail',
  templateUrl: 'api-connector-detail.component.html',
  styleUrls: ['api-connector-detail.component.scss']
})
export class ApiConnectorDetailComponent implements OnInit {
  apiConnector$: Observable<ApiConnector>;
  loading$: Observable<boolean>;
  integrationLengthMapping: { [valueComparator: string]: string } = {
    '=0': 'No integrations are using this extension.',
    '=1': 'Currently used by <strong>1</strong> integration.',
    'other': 'Currently used by <strong>#</strong> integrations.'
  };

  constructor(private apiConnectorStore: ApiConnectorStore,
              private router: Router,
              private route: ActivatedRoute) {
    this.loading$ = this.apiConnectorStore.loading;
    this.apiConnector$ = this.apiConnectorStore.resource;
  }

  ngOnInit() {
    this.route.paramMap.first(params => params.has('id')).subscribe(params => {
      const id = params.get('id');
      this.apiConnectorStore.load(id);
    });
  }
}
