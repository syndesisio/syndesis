import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { NavigationService } from '@syndesis/ui/common';
import { IntegrationStore } from '@syndesis/ui/store';
import { Integration } from '@syndesis/ui/model';
import { ChildAwarePage, CurrentFlow, FlowEvent } from '@syndesis/ui/integrations';
import { log, getCategory } from '@syndesis/ui/logging';

const category = getCategory('IntegrationsEditPage');

@Component({
  selector: 'syndesis-integrations-edit-page',
  templateUrl: './edit-page.component.html',
  styleUrls: ['./edit-page.component.scss']
})
export class IntegrationsEditPage extends ChildAwarePage
  implements OnInit, OnDestroy {
  integration: Observable<Integration>;
  readonly loading: Observable<boolean>;

  integrationSubscription: Subscription;
  routeSubscription: Subscription;
  childRouteSubscription: Subscription;
  flowSubscription: Subscription;
  urls: UrlSegment[];
  _canContinue = false;
  position: number;

  constructor(
    public currentFlow: CurrentFlow,
    public store: IntegrationStore,
    public route: ActivatedRoute,
    public router: Router,
    public nav: NavigationService
  ) {
    super(currentFlow, route, router);
    this.integration = this.store.resource;
    this.loading = this.store.loading;
    this.store.clear();
    this.flowSubscription = this.currentFlow.events.subscribe(
      (event: FlowEvent) => {
        this.handleFlowEvent(event);
      }
    );
  }

  getPageRow() {
    switch (this.currentStepKind) {
      case 'mapper':
        return 'row datamapper';
      default:
        return 'row';
    }
  }

  getSidebarClass() {
    switch (this.currentStepKind) {
      case 'mapper':
        return 'mapper-sidebar';
      default:
        return 'wizard-sidebar';
    }
  }

  getPageContainer() {
    switch (this.currentStepKind) {
      case 'mapper':
        return 'mapper-main';
      default:
        return 'wizard-main';
    }
  }

  handleFlowEvent(event: FlowEvent) {
    const child = this.getCurrentChild();
    let validate = false;
    switch (event.kind) {
      case 'integration-updated':
        if (!child) {
          validate = true;
        }
        break;
      case 'integration-no-connections':
        validate = true;
        break;
      case 'integration-no-actions':
      case 'integration-action-select':
      case 'integration-connection-select':
      case 'integration-selected-action':
      case 'integration-selected-connection':
      case 'integration-action-configure':
      case 'integration-connection-configure':
      default:
        break;
    }

    if (validate) {
      this.router.navigate(['save-or-add-step'], {
        queryParams: { validate: true },
        relativeTo: this.route
      });
    }
  }

  ngOnInit() {
    this.integrationSubscription = this.integration.subscribe(
      (i: Integration) => {
        if (i) {
          this.currentFlow.integration = i;
        }
      }
    );

    this.routeSubscription = this.route.paramMap
      .map(params => params.get('integrationId'))
      .subscribe(integrationId => this.store.loadOrCreate(integrationId));

    this.nav.hide();
  }

  ngOnDestroy() {
    this.nav.show();
    if (this.integrationSubscription) {
      this.integrationSubscription.unsubscribe();
    }
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
    if (this.flowSubscription) {
      this.flowSubscription.unsubscribe();
    }
  }
}
