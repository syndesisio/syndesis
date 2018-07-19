import { map } from 'rxjs/operators';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable, Subscription } from 'rxjs';

import { NavigationService } from '@syndesis/ui/common';
import { IntegrationStore } from '@syndesis/ui/store';
import { Integration } from '@syndesis/ui/platform';
import { FlowEvent } from '@syndesis/ui/integration/edit-page';
import { CurrentFlowService } from '@syndesis/ui/integration/edit-page/current-flow.service';
import { FlowPageService } from '@syndesis/ui/integration/edit-page/flow-page.service';
import { log, getCategory } from '@syndesis/ui/logging';

const category = getCategory('IntegrationsEditPage');

@Component({
  selector: 'syndesis-integration-edit-page',
  templateUrl: './edit-page.component.html',
  styleUrls: ['./edit-page.component.scss']
})
export class IntegrationEditPage implements OnInit, OnDestroy {
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
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public integrationStore: IntegrationStore,
    public route: ActivatedRoute,
    public router: Router,
    public navigationService: NavigationService
  ) {
    this.integration = this.integrationStore.resource;
    this.loading = this.integrationStore.loading;
    this.integrationStore.clear();
    this.flowSubscription = this.currentFlowService.events.subscribe(
      (event: FlowEvent) => {
        this.handleFlowEvent(event);
      }
    );
  }

  getPageRow() {
    switch (this.flowPageService.getCurrentStepKind(this.route)) {
      case 'mapper':
        return 'row datamapper';
      default:
        return 'row';
    }
  }

  getPageContainer() {
    switch (this.flowPageService.getCurrentStepKind(this.route)) {
      case 'mapper':
        return 'mapper-main';
      default:
        return 'wizard-main';
    }
  }

  handleFlowEvent(event: FlowEvent) {
    const child = this.flowPageService.getCurrentChild(this.route);
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
          this.currentFlowService.integration = i;
        }
      }
    );

    this.routeSubscription = this.route.paramMap
      .pipe(map(params => params.get('integrationId')))
      .subscribe(integrationId =>
        this.integrationStore.loadOrCreate(integrationId)
      );

    this.navigationService.hide();
  }

  ngOnDestroy() {
    this.navigationService.show();
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
