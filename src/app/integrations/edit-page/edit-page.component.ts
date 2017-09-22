import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { NavigationService } from '../../common/navigation.service';
import { IntegrationStore } from '../../store/integration/integration.store';
import { Integration } from '../../model';
import { CurrentFlow, FlowEvent } from './current-flow.service';
import { log, getCategory } from '../../logging';
import { ChildAwarePage } from './child-aware-page';

const category = getCategory('IntegrationsEditPage');

@Component({
  selector: 'syndesis-integrations-edit-page',
  templateUrl: './edit-page.component.html',
  styleUrls: ['./edit-page.component.scss'],
})
export class IntegrationsEditPage extends ChildAwarePage
  implements OnInit, OnDestroy {
  integration: Observable<Integration>;
  readonly loading: Observable<boolean>;

  integrationSubscription: Subscription;
  routeSubscription: Subscription;
  routerEventsSubscription: Subscription;
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
    public detector: ChangeDetectorRef,
    public nav: NavigationService,
  ) {
    super(currentFlow, route, router);
    this.integration = this.store.resource;
    this.loading = this.store.loading;
    this.store.clear();
    this.flowSubscription = this.currentFlow.events.subscribe(
      (event: FlowEvent) => {
        this.handleFlowEvent(event);
      },
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
    // TODO we could probably tidy up the unused cases at some point
    switch (event.kind) {
      case 'integration-updated':
        if (!child) {
          validate = true;
        }
        break;
      case 'integration-no-actions':
        break;
      case 'integration-no-connections':
        validate = true;
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
    try {
      this.detector.detectChanges();
    } catch (err) {}
    if (validate) {
      this.router.navigate(['save-or-add-step'], {
        queryParams: { validate: true },
        relativeTo: this.route,
      });
    }
  }

  ngOnInit() {
    this.routerEventsSubscription = this.router.events.subscribe(event => {
      try {
        this.detector.detectChanges();
      } catch (err) {
        // ignore;
      }
    });
    this.routeSubscription = this.route.params
      .pluck<Params, string>('integrationId')
      .map((integrationId: string) => {
        this.store.loadOrCreate(integrationId);
      })
      .subscribe();
    this.integrationSubscription = this.integration.subscribe(
      (i: Integration) => {
        if (i) {
          this.currentFlow.integration = i;
        }
      },
    );
    this.nav.hide();
  }

  ngOnDestroy() {
    this.nav.show();
    this.integrationSubscription.unsubscribe();
    this.routeSubscription.unsubscribe();
    if (this.flowSubscription) {
      this.flowSubscription.unsubscribe();
    }
    this.routerEventsSubscription.unsubscribe();
  }
}
