import { Component, Input, Output, EventEmitter } from '@angular/core';

import { ApiConnectorData, CustomConnectorRequest } from '@syndesis/ui/customizations/api-connector';

@Component({
  selector: 'syndesis-api-connector-review',
  templateUrl: './api-connector-review.component.html',
  styleUrls: ['./api-connector-review.component.scss']
})
export class ApiConnectorReviewComponent {
  validation: ApiConnectorData;
  importedActions: Array<{ tag: string; count: number }>;

  @Input() apiConnectorTemplateName: string;
  @Input() showNextButton: boolean;
  @Input() enableEditButton: boolean;
  @Input()
  set apiConnectorData(value: ApiConnectorData) {
    this.validation = value;
    const actionCountByTags = value.actionsSummary.actionCountByTags || {};
    this.importedActions = Object.keys(actionCountByTags).map(key => ({
      tag: key,
      count: +actionCountByTags[key]
    }));
  }

  @Output() backPressed = new EventEmitter();
  @Output() reviewComplete = new EventEmitter();

  onBackPressed() {
    this.backPressed.emit();
  }
}
