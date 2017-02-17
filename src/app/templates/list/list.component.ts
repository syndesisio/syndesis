import { Component, Input } from '@angular/core';

import { IntegrationTemplates } from '../../model';

@Component({
  selector: 'ipaas-templates-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss'],
})
export class TemplatesListComponent {

  @Input() templates: IntegrationTemplates;

  @Input() loading: boolean;

}
