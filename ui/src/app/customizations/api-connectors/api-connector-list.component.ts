import { Component } from '@angular/core';
import { log, getCategory } from '../../logging';

const category = getCategory('ApiConnectors');

@Component({
  selector: 'syndesis-api-connector-list',
  templateUrl: './api-connector-list.component.html',
  styleUrls: ['./api-connector-list.component.scss']
})
export class ApiConnectorListComponent {}
