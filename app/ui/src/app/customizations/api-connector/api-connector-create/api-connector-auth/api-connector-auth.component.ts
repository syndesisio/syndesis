import { Component, Output, OnInit, EventEmitter } from '@angular/core';

@Component({
  selector: 'syndesis-api-connector-auth',
  templateUrl: './api-connector-auth.component.html',
  styleUrls: ['./api-connector-auth.component.scss']
})
export class ApiConnectorAuthComponent implements OnInit {
  @Output() next = new EventEmitter();

  ngOnInit() {
    // TBD
  }
}
