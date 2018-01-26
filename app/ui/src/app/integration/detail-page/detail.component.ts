import {
  ApplicationRef,
  Component,
  OnInit,
  OnDestroy,
} from '@angular/core';

import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { Action as PFAction, ActionConfig, ListConfig, NotificationType } from 'patternfly-ng';

import { IntegrationStore, StepStore, EventsService } from '@syndesis/ui/store';
import { IntegrationSupportService } from '../integration-support.service';
import { Connection, Action } from '@syndesis/ui/model';
import { ACTIVE, Integration, Step, IntegrationDeployment } from '@syndesis/ui/integration';
import { IntegrationViewBase } from '../components';
import { ModalService, NotificationService } from '@syndesis/ui/common';
import { ConfigService } from '@syndesis/ui/config.service';

// menu items and buttons
const replaceDraft = {
  id: 'replaceDraft',
  title: 'Replace Draft',
  tooltip: 'Replace the current draft with this version'
} as PFAction;
const stopIntegration = {
  id: 'stopIntegration',
  title: 'Stop Integration',
  tooltip: 'Stop this integration'
} as PFAction;
const createDraft = {
  id: 'createDraft',
  title: 'Create Draft',
  tooltip: 'Create a new draft from this version'
} as PFAction;
const publish = {
  id: 'publish',
  title: 'Publish',
  tooltip: 'Publish this version of the integration'
} as PFAction;

@Component({
  selector: 'syndesis-integration-detail-page',
  templateUrl: 'detail.component.html',
  styleUrls: ['detail.component.scss']
})
export class IntegrationDetailComponent extends IntegrationViewBase
  implements OnInit, OnDestroy {
  integration$: Observable<Integration>;
  integrationDeployments$: Observable<Array<IntegrationDeployment>>;
  integrationSubscription: Subscription;
  eventsSubscription: Subscription;
  integration: Integration;
  readonly loading$: Observable<boolean>;
  routeSubscription: Subscription;
  loggingEnabled = false;
  usesMapping: { [valueComparator: string]: string } = {
    '=0': '0 Uses',
    '=1': '1 Use',
    'other': '# Uses'
  };
  deploymentListConfig: ListConfig;
  deploymentActionConfigs: { [id: string]: ActionConfig } = {};
  currentDeployment: IntegrationDeployment;

  constructor(
    public store: IntegrationStore,
    public stepStore: StepStore,
    public route: ActivatedRoute,
    public router: Router,
    public notificationService: NotificationService,
    public modalService: ModalService,
    public application: ApplicationRef,
    public integrationSupportService: IntegrationSupportService,
    private config: ConfigService,
    private eventsService: EventsService
  ) {
    super(store, route, router, notificationService, modalService, application, integrationSupportService);
    this.integration$ = this.store.resource;
    this.loading$ = this.store.loading;
  }

  viewDetails(step: Step) {
    if (step && step.connection) {
      this.router.navigate(['/connections/', step.connection.id]);
    }
  }

  getStepLineClass(index: number) {
    if (index === 0) {
      return 'start';
    }
    if (index === this.integration.steps.length - 1) {
      return 'finish';
    }
    return '';
  }

  deleteAction(integration: Integration) {
    return super
      .deleteAction(integration)
      .then(_ => this.router.navigate(['/integrations']));
  }

  attributeUpdated(attr: string, value: string) {
    this.integration[attr] = value;
    this.store
      .update(this.integration)
      .toPromise()
      .then((update: Integration) => {
        this.notificationService.popNotification({
          type: NotificationType.SUCCESS,
          header: 'Update Successful',
          message: `Updated ${attr}`
        });
      })
      .catch(reason => {
        this.notificationService.popNotification({
          type: NotificationType.WARNING,
          header: 'Update Failed',
          message: `Failed to update ${attr}: ${reason}`
        });
      });
  }

  deploymentAction($event, deployment) {
   //console.log('Deployment action: ', $event, deployment);
   // TODO
  }

  validateName(name: string) {
    return name && name.length > 0 ? null : 'Name is required';
  }

  exportIntegration() {
    super.requestAction('export', this.integration);
  }

  ngOnInit() {
    this.deploymentListConfig = {
      selectItems: false,
      showCheckbox: false,
      useExpandItems: true
    } as ListConfig;
    this.loggingEnabled = this.config.getSettings('features', 'logging', false);
    this.integrationSubscription = this.integration$
      .first( i => i.id !== undefined )
      .subscribe(i => {
        this.integration = i;
        this.integrationDeployments$ = this.integrationSupportService.watchDeployments(this.integration.id)
            .map(val => {
              this.deploymentActionConfigs = {};
              const answer = val.items.sort((a, b) => b.version - a.version);
              const integration = this.integration;
              // build our map of actions for all the deployments
              for (const deployment of answer) {
                const actionConfig = {
                  primaryActions: [],
                  moreActions: [],
                  moreActionsVisible: true
                } as ActionConfig;
                actionConfig.moreActions.push(replaceDraft);
                if (deployment.version === (integration.version || integration.deploymentId)) {
                  this.currentDeployment = deployment;
                  if (integration.currentStatus === ACTIVE) {
                    actionConfig.moreActions.push(stopIntegration);
                  } else {
                    actionConfig.moreActions.push(publish);
                  }
                } else {
                  actionConfig.moreActions.push(publish);
                }
                this.deploymentActionConfigs[deployment.id] = actionConfig;
              }
              return answer;
            });
    });
    this.routeSubscription = this.route.paramMap
      .first( params => params.has('integrationId'))
      .subscribe(paramMap => this.store.load(paramMap.get('integrationId')));
  }

  ngOnDestroy() {
    this.integrationSubscription.unsubscribe();
    this.routeSubscription.unsubscribe();
  }

}
