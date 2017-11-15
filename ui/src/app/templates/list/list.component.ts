import { Component, Input } from '@angular/core';

import { IntegrationTemplates } from '../../model';

@Component({
  selector: 'syndesis-templates-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class TemplatesListComponent {
  @Input() templates: IntegrationTemplates;

  @Input() loading: boolean;
}
