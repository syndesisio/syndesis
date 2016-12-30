import { Component, OnInit } from '@angular/core';

import { Observable } from 'rxjs/Observable';

import { IntegrationsService } from '../../store/model/integrations.service';
import { Integrations } from '../../store/model/integration.model';

@Component({
  selector: 'ipaas-integrations-list-page',
  templateUrl: './list-page.component.html',
  styleUrls: ['./list-page.component.scss']
})
export class IntegrationsListPage implements OnInit {

  integrations: Observable<Integrations>;

  constructor(private integrationsService: IntegrationsService) { }

  ngOnInit() {
    this.integrations = this.integrationsService.list();
  }

}
