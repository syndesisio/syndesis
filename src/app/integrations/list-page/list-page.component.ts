import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { IntegrationStore } from '../../store/integration/integration.store';
import { Integrations } from '../../store/integration/integration.model';

@Component({
  selector: 'ipaas-integrations-list-page',
  templateUrl: './list-page.component.html',
  styleUrls: ['./list-page.component.scss'],
})
export class IntegrationsListPage implements OnInit {

  integrations: Observable<Integrations>;
  loading: Observable<boolean>;

  constructor(private store: IntegrationStore) {
    this.integrations = this.store.list;
    this.loading = this.store.loading;
  }

  ngOnInit() {
    this.store.loadAll();
  }

}
