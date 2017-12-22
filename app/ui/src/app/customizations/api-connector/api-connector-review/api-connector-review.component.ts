import { Component, Input, Output, EventEmitter } from '@angular/core';

import { ApiConnectorValidation } from '@syndesis/ui/customizations/api-connector';

@Component({
  selector: 'syndesis-api-connector-review',
  templateUrl: './api-connector-review.component.html',
  styleUrls: ['./api-connector-review.component.scss']
})
export class ApiConnectorReviewComponent {
  validation: ApiConnectorValidation;
  importedActions: Array<{ tag: string; count: number; }>;

  @Input() apiConnectorTemplateName: string;
  @Input() showNextButton: boolean;
  @Input() set apiConnectorValidation(value: ApiConnectorValidation) {
    this.validation = value;
    const actionCountByTags = value.actionsSummary.actionCountByTags;
    this.importedActions = Object.keys(actionCountByTags).map(key => ({
      tag: key,
      count: +actionCountByTags[key]
    }));

  }

  @Output() reviewComplete = new EventEmitter();
}
