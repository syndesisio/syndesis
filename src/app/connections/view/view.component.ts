import { Component, Input } from '@angular/core';

import { Connection } from '../../store/connection/connection.model';

@Component({
  selector: 'ipaas-connection-view',
  templateUrl: './view.component.html',
  styleUrls: ['./view.component.scss'],
})
export class ConnectionViewComponent {

  @Input() connection: Connection;

}
