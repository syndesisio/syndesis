import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ApiConnectorData } from '@syndesis/ui/customizations/api-connector';

@Component({
  selector: 'syndesis-api-connector-review',
  templateUrl: './api-connector-review.component.html',
  styleUrls: ['./api-connector-review.component.scss']
})
export class ApiConnectorReviewComponent {

  @Input() apiConnectorTemplateName: string;
  @Input() enableEditButton: boolean;
  @Input() loading: boolean;
  @Input() showNextButton: boolean;
  @Input() validation: ApiConnectorData;

  @Output() onBack = new EventEmitter<boolean>();
  @Output() onDone = new EventEmitter<boolean>();
  @Output() onEdit = new EventEmitter<boolean>();

}
