import { Component, Input, TemplateRef } from '@angular/core';

@Component({
  selector: 'syndesis-loading',
  templateUrl: './loading.component.html',
  styleUrls: ['./loading.component.scss']
})
export class LoadingComponent {
  @Input() loading: boolean;
  @Input() content: TemplateRef<any>;
}
