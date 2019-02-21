import { Component, Input } from '@angular/core';
import { Integration } from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-flow-view-cancel-prompt',
  templateUrl: './flow-view-cancel-prompt.component.html',
})
export class FlowViewCancelPromptComponent {
  @Input() integration: Integration;
}
