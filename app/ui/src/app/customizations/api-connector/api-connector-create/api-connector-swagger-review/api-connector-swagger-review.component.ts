import { Component, OnInit, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'syndesis-api-connector-swagger-review',
  templateUrl: './api-connector-swagger-review.component.html',
  styleUrls: ['./api-connector-swagger-review.component.scss']
})
export class ApiConnectorSwaggerReviewComponent {
  @Output() next = new EventEmitter();
}
