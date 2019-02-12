import { Component, Input } from '@angular/core';

@Component({
  selector: 'integration-api-provider-operation-description',
  template: `
    <span class="verb" [attr.data-verb]="description.split(' ')[0]">
      {{ description.split(' ')[0] }}
    </span>
    <span class="url">{{ description.split(' ')[1] }}</span>
  `,
  styles: [`
    :host {
      display: flex;
    }
    .verb {
      font-weight: bold;
      flex-grow: 0;
      flex-shrink: 1;
      flex-basis: 30%;
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
    .url {
      padding-left: 5%;
    }
  `]
})
export class ApiProviderOperationDescriptionComponent {
  @Input() description: string;
}
