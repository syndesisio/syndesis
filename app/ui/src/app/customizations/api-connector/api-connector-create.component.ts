import { Component, OnInit } from '@angular/core';
import { ConnectorStore } from '../../store/connector/connector.store';

@Component({
  selector: 'syndesis-api-connector-create',
  templateUrl: 'api-connector-create.component.html'
})

export class ApiConnectorCreateComponent implements OnInit {
  constructor(private store: ConnectorStore) { }

  ngOnInit() {
    // Happening soon!
  }
}
