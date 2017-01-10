import { Component, Input } from '@angular/core';

import { Integrations } from '../../store/integration/integration.model';

@Component({
  selector: 'ipaas-integrations-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss'],
})
export class IntegrationsListComponent {

  @Input() integrations: Integrations;

  @Input() loading: boolean;

}
