import { Component, Input } from '@angular/core';

import { Connections } from '../../store/connection/connection.model';

@Component({
  selector: 'ipaas-connections-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss'],
})
export class ConnectionsListComponent {

  truncateLimit = 80;

  truncateTrail = '…';

  @Input() connections: Connections;

  @Input() loading: boolean;

}
