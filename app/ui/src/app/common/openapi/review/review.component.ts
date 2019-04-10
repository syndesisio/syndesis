import { Component, Input } from '@angular/core';
import {
  OpenApiValidationActionsSummary,
  OpenApiValidationErrors,
  OpenApiValidationWarnings
} from '@syndesis/ui/common';

@Component({
  selector: 'openapi-review',
  templateUrl: './review.component.html',
  styleUrls: ['./review.component.scss']
})
export class OpenApiReviewComponent {
  actionsSummary: OpenApiValidationActionsSummary;
  importedActions: Array<{ tag: string; count: number }>;

  @Input() apiConnectorTemplateName: string;
  @Input() showNextButton: boolean;
  @Input() enableEditButton: boolean;
  @Input() name: string;
  @Input() description: string;
  @Input() warnings: OpenApiValidationWarnings;
  @Input() errors: OpenApiValidationErrors;
  @Input('actionsSummary')
  set validatorData(value: OpenApiValidationActionsSummary) {
    this.actionsSummary = value;
    const actionCountByTags = this.actionsSummary && this.actionsSummary.actionCountByTags || {};
    this.importedActions = Object.keys(actionCountByTags).map(key => ({
      tag: key,
      count: +actionCountByTags[key]
    }));
  }
}
