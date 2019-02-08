import { Component, Input } from '@angular/core';

@Component({
  selector: 'integration-api-provider-operation-description',
  template: `
    <span class="verb col-sm-3" [attr.data-verb]="description.split(' ')[0]">
      {{ description.split(' ')[0] }}
    </span>
    <span class="url col-sm-9">{{ description.split(' ')[1] }}</span>
  `,
  styles: [`
    .verb {
      font-weight: bold;
    }
    .verb[data-verb=GET] {
      color: #3C96D4;
    }
    .verb[data-verb=POST] {
      color: #61AF5A;
    }
    .verb[data-verb=PUT] {
      color: #ED811D;
    }
    .verb[data-verb=DELETE] {
      color: #D01414;
    }
  `]
})
export class ApiProviderOperationDescriptionComponent {
  @Input() description: string;
}
