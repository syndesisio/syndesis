import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ApiConnectorData } from './api-connector.models';
import { ApiConnectorService } from './api-connector.service';

@Component({
  selector: 'syndesis-api-connector-detail',
  templateUrl: 'api-connector-detail.component.html',
  styleUrls: ['api-connector-detail.component.scss']
})
export class ApiConnectorDetailComponent implements OnInit {
  apiConnectorData: ApiConnectorData;
  loading: boolean;

  constructor(private apiConnectorService: ApiConnectorService,
              private route: ActivatedRoute) {
    this.loading = true;
  }

  ngOnInit() {
    this.route.paramMap
      .first(params => params.has('id'))
      .map(params => params.get('id'))
      .switchMap(id => this.apiConnectorService.getApiConnector(id))
      .subscribe(apiConnectorData => {
        this.apiConnectorData = apiConnectorData;
        this.loading = false;
      });
  }
}
