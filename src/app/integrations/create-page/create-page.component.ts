import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { IntegrationStore } from '../../store/integration/integration.store';
import { Integration } from '../../store/integration/integration.model';

import { log, getCategory } from '../../logging';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'ipaas-integrations-create-page',
  templateUrl: './create-page.component.html',
  styleUrls: ['./create-page.component.scss'],
})
export class IntegrationsCreatePage implements OnInit, OnDestroy {

  integration: Observable<Integration>;
  private readonly loading: Observable<boolean>;

  subscription: Subscription;

  constructor(private store: IntegrationStore,
    private route: ActivatedRoute,
    private router: Router) {
    this.integration = this.store.resource;
    this.loading = this.store.loading;
  }

  ngOnInit() {
    this.subscription = this.route.params.pluck<Params, string>('integrationId')
      .map((integrationId: string) => this.store.loadOrCreate(integrationId))
      .subscribe();
    this.integration.subscribe((i: Integration) => {
      log.debugc(() => 'Integration: ' + JSON.stringify(i, null, 2), category);
      if (!i.steps || !i.steps.length || i.steps.length > 2) {
        this.router.navigate(['connection-select', 1], { relativeTo: this.route });
      }
    });
  }

  ngOnDestroy() { this.subscription.unsubscribe(); }

}
