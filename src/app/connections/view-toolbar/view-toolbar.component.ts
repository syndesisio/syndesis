import { Component, Input } from '@angular/core';

import { Connection } from '../../store/connection/connection.model';

@Component({
  selector: 'ipaas-connection-view-toolbar',
  templateUrl: './view-toolbar.component.html',
  styleUrls: ['./view-toolbar.component.scss'],
})
export class ConnectionViewToolbarComponent {

  @Input() connection: Connection;

}
