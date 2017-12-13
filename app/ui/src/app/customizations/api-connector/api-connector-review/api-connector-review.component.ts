import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

import { ApiConnectorValidation } from '@syndesis/ui/customizations/api-connector';

@Component({
  selector: 'syndesis-api-connector-review',
  templateUrl: './api-connector-review.component.html',
  styleUrls: ['./api-connector-review.component.scss']
})
export class ApiConnectorReviewComponent {
  @Input() apiConnectorTemplateName: string;
  @Input() apiConnectorValidation: ApiConnectorValidation;
  @Input() showNext: boolean;

  @Output() next = new EventEmitter();
}
