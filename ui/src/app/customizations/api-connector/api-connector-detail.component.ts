import { Component, OnInit } from '@angular/core';
import { ConnectorStore } from '../../store/connector/connector.store';

@Component({
  selector: 'syndesis-api-connector-detail',
  templateUrl: 'api-connector-detail.component.html'
})
export class ApiConnectorDetailComponent implements OnInit {
  constructor(private store: ConnectorStore) { }

  ngOnInit() {
    // Happening soon!
  }
}
