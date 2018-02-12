import { ApplicationRef, Component, OnInit, OnDestroy } from '@angular/core';

import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { Action as PFAction, ActionConfig, ListConfig, NotificationType } from 'patternfly-ng';

import { IntegrationStore, StepStore, EventsService } from '@syndesis/ui/store';
import { Integration,
  Step,
  PUBLISHED,
  UNPUBLISHED,
  IntegrationOverview,
  IntegrationActionsService,
  IntegrationSupportService,
  IntegrationDeployment } from '@syndesis/ui/platform';
import { ModalService, NotificationService } from '@syndesis/ui/common';
import { ConfigService } from '@syndesis/ui/config.service';

const REPLACE_DRAFT = 'replaceDraft';
const STOP_INTEGRATION = 'stopIntegration';
const CREATE_DRAFT = 'createDraft';
const PUBLISH = 'publish';
// menu items and buttons
const replaceDraft = {
  id: REPLACE_DRAFT,
  title: 'Replace Draft',
  tooltip: 'Replace the current draft with this version'
} as PFAction;
const stopIntegration = {
  id: STOP_INTEGRATION,
  title: 'Stop Integration',
  tooltip: 'Stop this integration'
} as PFAction;
const createDraft = {
  id: CREATE_DRAFT,
  title: 'Create Draft',
  tooltip: 'Create a new draft from this version'
} as PFAction;
const publish = {
  id: PUBLISH,
  title: 'Publish',
  tooltip: 'Publish this version of the integration'
} as PFAction;

@Component({
  selector: 'syndesis-integration-detail-page',
  templateUrl: 'detail.component.html',
  styleUrls: ['detail.component.scss']
})
export class IntegrationDetailComponent implements OnInit, OnDestroy {
  integration$: Observable<IntegrationOverview>;
  integrationDeployments$: Observable<Array<IntegrationDeployment>>;
  integrationSubscription: Subscription;
  eventsSubscription: Subscription;
  integration: IntegrationOverview;
  loading = true;
  routeSubscription: Subscription;
  loggingEnabled = false;
  usesMapping: { [valueComparator: string]: string } = {
    '=0': '0 Uses',
    '=1': '1 Use',
    'other': '# Uses'
  };
  deploymentListConfig: ListConfig;
  deploymentActionConfigs: { [id: string]: ActionConfig } = {};
  draftConfig: ActionConfig;
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
    public integrationActionsService: IntegrationActionsService,
    private config: ConfigService,
    private eventsService: EventsService,

  ) {

  }

  get modalTitle() {
    return this.integrationActionsService.getModalTitle();
  }

  get modalMessage() {
    return this.integrationActionsService.getModalMessage();
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
    if (index === (<any>this.integration).steps.length - 1) {
      return 'finish';
    }
    return '';
  }

  attributeUpdated(attr: string, value: string) {
    const attributes = {};
    attributes[attr] = value;
    this.store
      .patch( <any> this.integration, attributes)
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

  draftAction(event) {
    switch (event.id) {
      case 'publish':
        this.integrationActionsService.requestAction('publish', <any> this.integration);
        break;
      case 'edit':
        this.integrationActionsService.requestAction('edit', <any> this.integration);
        break;
      default:
        break;
    }
  }

  deploymentAction(event, deployment: IntegrationDeployment) {
    switch (event.id) {
      case REPLACE_DRAFT:
        this.integrationActionsService.requestAction('replaceDraft', this.integration, deployment);
        break;
      case CREATE_DRAFT:
        // TODO doesn't this just mean edit?
        break;
      case STOP_INTEGRATION:
        this.integrationActionsService.requestAction('deactivate', <any> this.integration);
        break;
      case PUBLISH:
        {
          const integration = {
            integrationVersion: this.integration.version,
            id: this.integration.id,
            name: this.integration.name,
            version: deployment.version
           };
          this.integrationActionsService.requestAction('publish', <any> integration);
        }
        break;
      default:
    }
  }

  validateName(name: string) {
    return name && name.length > 0 ? null : 'Name is required';
  }

  ngOnInit() {
    this.draftConfig = {
      primaryActions: [
        {... publish, ...{ styleClass: 'btn btn-default primary-action' } },
        {
          id: 'edit',
          title: 'Edit',
          styleClass: 'btn btn-default primary-action',
          tooltip: 'Edit this draft'
        } as PFAction,
      ]
    } as ActionConfig;
    this.deploymentListConfig = {
      selectItems: false,
      showCheckbox: false,
      useExpandItems: true
    } as ListConfig;
    this.loggingEnabled = this.config.getSettings('features', 'logging', false);
    this.routeSubscription = this.route.paramMap
      .first( params => params.has('integrationId'))
      .subscribe(paramMap => {
        if (this.integrationSubscription) {
          this.integrationSubscription.unsubscribe();
        }
        this.integration$ = this.integrationSupportService.watchOverview(paramMap.get('integrationId'));
        this.integrationSubscription = this.integration$.subscribe((integration: IntegrationOverview) => {
          this.loading = false;
          this.integration = integration;
          this.deploymentActionConfigs = {};
          for (const deployment of this.integration.deployments) {
            const actionConfig = {
              primaryActions: [],
              moreActions: [],
              moreActionsVisible: true,
              moreActionsDisabled: false
            } as ActionConfig;
            actionConfig.moreActions.push(replaceDraft);
            if (deployment.version === integration.deploymentVersion) {
              if (integration.currentState === PUBLISHED) {
                actionConfig.moreActions.push(stopIntegration);
              } else {
                actionConfig.moreActions.push(publish);
              }
            } else {
              actionConfig.moreActions.push(publish);
            }
            this.deploymentActionConfigs[deployment.id] = actionConfig;
          }
        });
      });
  }

  ngOnDestroy() {
    if (this.integrationSubscription) {
    this.integrationSubscription.unsubscribe();
    }
    this.routeSubscription.unsubscribe();
  }

}
