import { Component, OnInit, Input } from '@angular/core';

import { Observable } from 'rxjs/Observable';

import { Integrations } from '../../store/model/integration.model';

@Component({
  selector: 'ipaas-integrations-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class IntegrationsListComponent {

  @Input() errorMessage: string;

  @Input() integrations: Integrations;

}
