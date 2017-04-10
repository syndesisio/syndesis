import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { IntegrationStore } from '../../store/integration/integration.store';
import { Integration } from '../../model';
import { CurrentFlow, FlowEvent } from './current-flow.service';
import { log, getCategory } from '../../logging';
import { ChildAwarePage } from './child-aware-page';

const category = getCategory('IntegrationsEditPage');

@Component({
  selector: 'ipaas-integrations-edit-page',
  templateUrl: './edit-page.component.html',
  styleUrls: [ './edit-page.component.scss' ],
})
export class IntegrationsEditPage extends ChildAwarePage implements OnInit, OnDestroy {

  integration: Observable<Integration>;
  private readonly loading: Observable<boolean>;

  integrationSubscription: Subscription;
  routeSubscription: Subscription;
  childRouteSubscription: Subscription;
  flowSubscription: Subscription;
  urls: UrlSegment[];
  _canContinue = false;
  position: number;

  constructor( public currentFlow: CurrentFlow,
              public store: IntegrationStore,
              public route: ActivatedRoute,
              public router: Router,
              public detector: ChangeDetectorRef,
               ) {
    super(currentFlow, route, router);
    this.integration = this.store.resource;
    this.loading = this.store.loading;
    this.flowSubscription = this.currentFlow.events.subscribe((event: FlowEvent) => {
      this.handleFlowEvent(event);
    });
  }

  getPageRow() {
    switch (this.currentStepKind) {
      case 'mapper':
        return 'row datamapper';
      default:
        return 'wizard-pf-row';
    }
  }

  getSidebarClass() {
    switch (this.currentStepKind) {
      case 'mapper':
        return 'col-md-1';
      default:
        return 'wizard-pf-sidebar';
    }
  }

  getPageContainer() {
    switch (this.currentStepKind) {
      case 'mapper':
        return 'col-md-11';
      default:
        return 'wizard-pf-main';
    }
  }

  handleFlowEvent(event: FlowEvent) {
    const child = this.getCurrentChild();
    // TODO we could probably tidy up the unused cases at some point
    switch (event.kind) {
      case 'integration-updated':
        this.router.navigate(['save-or-add-step'], { relativeTo: this.route });
        this.detector.detectChanges();
        break;
      case 'integration-no-actions':
        break;
      case 'integration-no-connections':
        break;
      case 'integration-action-select':
      case 'integration-connection-select':
        break;
      case 'integration-selected-action':
        break;
      case 'integration-selected-connection':
        break;
      case 'integration-action-configure':
      case 'integration-connection-configure':
        break;
    }
  }

  ngOnInit() {
    this.integrationSubscription = this.integration.subscribe((i: Integration) => {
      this.currentFlow.integration = i;
    });
    this.routeSubscription = this.route.params.pluck<Params, string>('integrationId')
      .map((integrationId: string) => this.store.loadOrCreate(integrationId))
      .subscribe();
    $.fn.setupVerticalNavigation ? $.fn.setupVerticalNavigation().hideMenu() : '';
  }

  ngOnDestroy() {
    this.integrationSubscription.unsubscribe();
    this.routeSubscription.unsubscribe();
    if (this.flowSubscription) {
      this.flowSubscription.unsubscribe();
    }
    $.fn.setupVerticalNavigation ? $.fn.setupVerticalNavigation().showMenu() : '';
  }

}
