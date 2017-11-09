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
import { TourService } from 'ngx-tour-ngx-bootstrap';
import { UserService } from '../../common/user.service';

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
    public tourService: TourService,
    private userService: UserService,
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
    this.integrationSubscription = this.integration.subscribe(
      (i: Integration) => {
        if (i) {
          this.currentFlow.integration = i;
          /**
           * If guided tour state is set to be shown (i.e. true), then show it for this page, otherwise don't.
           */
          /*
          if (this.userService.getTourState() === true) {
            this.tourService.initialize(
              [
                {
                  route: 'integrations/create/connection-select/0',
                  title: 'Available Connections',
                  content:
                    'After at least two connections are available, you can create an integration that uses the connections you choose.',
                  anchorId: 'integrations.connections',
                  placement: 'top',
                },
                {
                  route: 'integrations/create/connection-select/0',
                  title: 'Integration Panel',
                  content:
                    'As you create an integration, see its connections and steps in the order ' +
                    'in which they occur when the integration is running.',
                  anchorId: 'integrations.panel',
                  placement: 'right',
                },
                {
                  route: 'integrations/create/action-select/0',
                  title: 'Available Actions',
                  content:
                    'When an integration uses the selected connection it performs the action you select.',
                  anchorId: 'integrations.actions',
                  placement: 'top',
                },
                {
                  route: 'integrations/create/action-configure/0/0',
                  title: 'Done',
                  content:
                    'Clicking Done adds the finish connection to the integration. ' +
                    'You can then add one or more steps that operate on the data.',
                  anchorId: 'integrations.done',
                  placement: 'bottom',
                },
                {
                  route: 'integrations/create/save-or-add-step?validate=true',
                  title: 'Operate On Data',
                  content:
                    'Clicking the plus sign lets you add an operation that ' +
                    'the integration performs between the start and finish connections.',
                  anchorId: 'integrations.step',
                  placement: 'right',
                },
                {
                  route: 'integrations/create/integration-basics',
                  title: 'Publish',
                  content:
                    'Click Publish to start running the integration, which will take a moment or two. ' +
                    'Click Save as Draft to save the integration without deploying it.',
                  anchorId: 'integrations.publish',
                  placement: 'bottom',
                },
              ],
              {
                route: '',
              },
            );
            this.tourService.start();
          }
          */
        }
      },
    );
    this.routeSubscription = this.route.params
      .pluck<Params, string>('integrationId')
      .map((integrationId: string) => {
        this.store.loadOrCreate(integrationId);
      })
      .subscribe();
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
    if (this.routerEventsSubscription) {
      this.routerEventsSubscription.unsubscribe();
    }
  }
}
