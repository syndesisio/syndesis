import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';

import { ApiConnectorData } from '@syndesis/ui/customizations/api-connector';

@Component({
  selector: 'syndesis-api-connector-review',
  templateUrl: './api-connector-review.component.html',
  styleUrls: ['./api-connector-review.component.scss']
})
export class ApiConnectorReviewComponent {
  validation: ApiConnectorData;
  importedActions: Array<{ tag: string; count: number }>;
  fromUrl = false;

  @Input() apiConnectorTemplateName: string;
  @Input() showNextButton: boolean;
  @Input()
  set apiConnectorData(value: ApiConnectorData) {
    this.validation = value;
    const actionCountByTags = value.actionsSummary.actionCountByTags || {};
    this.importedActions = Object.keys(actionCountByTags).map(key => ({
      tag: key,
      count: +actionCountByTags[key]
    }));
  }

  @Output() reviewComplete = new EventEmitter();

  ngOnInit() {
    this.checkIfFromUrl();
  }

  checkIfFromUrl() {
    if (this.validation.configuredProperties && this.validation.configuredProperties.specification) {
      this.fromUrl = true;
    }
  }
}
