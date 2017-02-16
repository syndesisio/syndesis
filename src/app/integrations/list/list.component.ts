import { Component, Input } from '@angular/core';

import { Integrations, Integration } from '../../store/integration/integration.model';

@Component({
  selector: 'ipaas-integrations-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss'],
})
export class IntegrationsListComponent {

  @Input() integrations: Integrations;

  @Input() loading: boolean;

  // TODO this is all super last-minute hacky
  private getConnection(integration: Integration, position: number) {
    if (integration) {
      return (integration.connections || [])[position];
    } else {
      return undefined;
    }
  }

  getStartIcon(integration: Integration) {
    const connection = this.getConnection(integration, 0);
    return (connection || {})['icon'] || 'fa-plane';
  }

  getFinishIcon(integration: Integration) {
    // TODO should be the last connection
    const connection = this.getConnection(integration, 1);
    return (connection || {})['icon'] || 'fa-plane';
  }

}
