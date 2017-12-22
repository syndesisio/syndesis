import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ApiConnectorData } from './api-connector.models';
import { ApiConnectorStore } from './api-connector.store';
import { ApiConnectorService } from '@syndesis/ui/customizations/api-connector/api-connector.service';


@Component({
  selector: 'syndesis-api-connector-detail',
  templateUrl: 'api-connector-detail.component.html',
  styleUrls: ['api-connector-detail.component.scss']
})
export class ApiConnectorDetailComponent implements OnInit {
  apiConnector: ApiConnectorData;
  loading: boolean;

  constructor(private apiConnectorStore: ApiConnectorStore,
              private apiConnectorService: ApiConnectorService,
              private route: ActivatedRoute) {
    this.loading = true;
  }

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');

    if(id) {
      this.apiConnectorService.getApiConnector(id).subscribe(apiConnectorData => {
        this.apiConnector = apiConnectorData;
        this.loading = false;
      });
    }
  }
}
