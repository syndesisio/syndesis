import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { Integration } from '../../model';
import { IntegrationStore } from '../../store/integration/integration.store';

@Component({
  selector: 'syndesis-integration-detail-page',
  templateUrl: 'detail.component.html',
  styleUrls: ['detail.component.scss'],
})
export class IntegrationsDetailComponent implements OnInit, OnDestroy {
  integration: Observable<Integration>;
  integrationSubscription: Subscription;
  i: Integration;
  private readonly loading: Observable<boolean>;
  routeSubscription: Subscription;

  constructor(
    public store: IntegrationStore,
    public route: ActivatedRoute,
    public router: Router,
  ) {
    this.integration = this.store.resource;
    this.loading = this.store.loading;
  }

  ngOnInit() {
    this.integrationSubscription = this.integration.subscribe(
      (i: Integration) => {
        this.i = i;
      },
    );
    this.routeSubscription = this.route.params
      .pluck<Params, string>('integrationId')
      .map((id: string) => this.store.load(id))
      .subscribe();
  }

  ngOnDestroy() {
    this.integrationSubscription.unsubscribe();
    this.routeSubscription.unsubscribe();
  }
}
