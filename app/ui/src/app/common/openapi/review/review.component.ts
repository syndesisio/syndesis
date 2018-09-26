import { Component, Input } from '@angular/core';
import { OpenApiValidationResponse } from '@syndesis/ui/common';

@Component({
  selector: 'openapi-review',
  templateUrl: './review.component.html',
  styleUrls: ['./review.component.scss']
})
export class OpenApiReviewComponent {
  validation: OpenApiValidationResponse;
  importedActions: Array<{ tag: string; count: number }>;

  @Input() apiConnectorTemplateName: string;
  @Input() showNextButton: boolean;
  @Input() enableEditButton: boolean;
  @Input()
  set validatorData(value: OpenApiValidationResponse) {
    this.validation = value;
    const actionCountByTags = value.actionsSummary.actionCountByTags || {};
    this.importedActions = Object.keys(actionCountByTags).map(key => ({
      tag: key,
      count: +actionCountByTags[key]
    }));
  }
}
