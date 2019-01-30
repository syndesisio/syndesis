import { ApplicationRef, Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, of, Subscription } from 'rxjs';
import { map, switchMap, combineLatest, first, filter } from 'rxjs/operators';
import {
  Action as PFAction,
  ActionConfig,
  NotificationType
} from 'patternfly-ng';

import { IntegrationStore, StepStore } from '@syndesis/ui/store';
import {
  Integration,
  Step,
  PUBLISHED,
  IntegrationOverview,
  IntegrationActionsService,
  IntegrationSupportService,
  IntegrationDeployment,
  IntegrationMetrics,
  IntegrationActions,
  PlatformState
} from '@syndesis/ui/platform';
import { ModalService, NotificationService } from '@syndesis/ui/common';
import { ConfigService } from '@syndesis/ui/config.service';
import { log } from '@syndesis/ui/logging';

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
  title: 'Unpublish',
  tooltip: 'Unpublish this integration'
} as PFAction;
const publish = {
  id: PUBLISH,
  title: 'Publish',
  tooltip: 'Publish this version of the integration'
} as PFAction;

@Component({
  selector: 'syndesis-integration-detail-page',
  templateUrl: 'integration-detail.component.html',
  styleUrls: ['../integration-common.scss', 'integration-detail.component.scss']
})
export class IntegrationDetailComponent implements OnInit, OnDestroy {
  integrationMetrics$: Observable<IntegrationMetrics>;
  integrationDeployments$: Observable<IntegrationDeployment[]>;
  integrationSubscription: Subscription;
  eventsSubscription: Subscription;
  integration: IntegrationOverview;
  loading = true;
  routeSubscription: Subscription;
  loggingEnabled = false;
  deploymentActionConfigs: { [id: string]: ActionConfig } = {};
  draftConfig: ActionConfig;
  currentDeployment: IntegrationDeployment;
  selectedTabbedView$: Observable<string>;

  constructor(
    public integrationStore: IntegrationStore,
    public stepStore: StepStore,
    public route: ActivatedRoute,
    public router: Router,
    public notificationService: NotificationService,
    public modalService: ModalService,
    public application: ApplicationRef,
    public integrationSupportService: IntegrationSupportService,
    public integrationActionsService: IntegrationActionsService,
    private config: ConfigService,
    private platformStore: Store<PlatformState>
  ) {}

  get modalTitle() {
    return this.integrationActionsService.getModalTitle();
  }

  get modalMessage() {
    return this.integrationActionsService.getModalMessage();
  }

  get modalType() {
    return this.integrationActionsService.getModalType();
  }

  get modalPrimaryText() {
    return this.integrationActionsService.getModalPrimaryText();
  }

  viewDetails(step: Step) {
    if (step && step.connection) {
      this.router.navigate(['/connections/', step.connection.id]);
    }
  }

  viewApiProviderOperations(integration: Integration) {
    if (integration) {
      this.router.navigate(['/integrations/', integration.id, 'operations']);
    }
  }

  nameUpdated(id: string, $event) {
    this.attributeUpdated(id, { name: $event });
  }

  attributeUpdated(id: string, updatedAttribute: { [key: string]: string }) {
    this.integrationStore
      .patch(id, updatedAttribute)
      .toPromise()
      .then((update: Integration) => {
        // silently succeed
      })
      .catch(reason => {
        this.notificationService.popNotification({
          type: NotificationType.WARNING,
          header: 'Update Failed',
          message: `Failed to update attribute: ${reason}`
        });
      });
  }

  draftAction(eventId: string) {
    switch (eventId) {
      case 'publish':
        this.integrationActionsService.requestAction('publish', <any>(
          this.integration
        ));
        break;
      case 'edit':
        this.integrationActionsService.requestAction('edit', <any>(
          this.integration
        ));
        break;
      default:
        break;
    }
  }

  deploymentAction(event: { id: string; deployment: IntegrationDeployment }) {
    switch (event.id) {
      case REPLACE_DRAFT:
        this.integrationActionsService.requestAction(
          'replaceDraft',
          this.integration,
          event.deployment
        );
        break;
      case STOP_INTEGRATION:
        this.integrationActionsService.requestAction('unpublish', <any>(
          this.integration
        ));
        break;
      case PUBLISH:
        {
          const integration = {
            integrationVersion: this.integration.version,
            id: this.integration.id,
            name: this.integration.name,
            version: event.deployment.version
          };
          this.integrationActionsService.requestAction('publish', <any>(
            integration
          ));
        }
        break;
      case CREATE_DRAFT:
      default:
        break;
    }
  }

  validateName(name: string) {
    return name && name.length > 0 ? null : 'Name is required';
  }

  ngOnInit() {
    this.integrationMetrics$ = this.platformStore
      .select('integrationState')
      .pipe(
        map(integrationState => integrationState.metrics.list),
        // tslint:disable-next-line:deprecation
        combineLatest(
          this.route.paramMap.pipe(
            first(params => params.has('integrationId')),
            map(paramMap => paramMap.get('integrationId'))
          )
        ),
        switchMap(([integrationMetrics, integrationId]) =>
          of(integrationMetrics.find(metrics => metrics.id === integrationId))
        )
      );

    this.selectedTabbedView$ = this.route.queryParams.pipe(
      map(params => params['view'] || 'description')
    );

    this.draftConfig = {
      primaryActions: [
        { ...publish, ...{ styleClass: 'btn btn-default primary-action' } },
        {
          id: 'edit',
          title: 'Edit',
          styleClass: 'btn btn-default primary-action',
          tooltip: 'Edit this draft'
        } as PFAction
      ]
    } as ActionConfig;

    this.loggingEnabled = this.config.getSettings('features', 'logging', false);
    this.routeSubscription = this.route.paramMap
      .pipe(first(params => params.has('integrationId')))
      .subscribe(paramMap => {
        if (this.integrationSubscription) {
          this.integrationSubscription.unsubscribe();
        }
        const integrationId = paramMap.get('integrationId');

        this.onRefreshMetrics(integrationId);

        this.integrationSubscription = this.integrationStore.resource
          .pipe(filter(i => typeof i !== 'undefined'))
          .subscribe(
            (integration: IntegrationOverview) => {
              this.loading = false;
              this.integration = integration;
              this.deploymentActionConfigs = {};
              if (!this.integration.deployments) {
                return;
              }
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
            },
            error => {
              if (error.status === 404) {
                this.router.navigate(['..'], { relativeTo: this.route });
                return;
              }
              log.warn('Error loading integration: ' + JSON.stringify(error));
            }
          );
        this.loading = true;
        this.integrationStore.load(integrationId);
      });
  }

  ngOnDestroy() {
    if (this.integrationSubscription) {
      this.integrationSubscription.unsubscribe();
    }
    this.routeSubscription.unsubscribe();
  }

  onRefreshMetrics(integrationId: string): void {
    if (integrationId || this.integration.id) {
      this.platformStore.dispatch(
        new IntegrationActions.FetchMetrics(
          integrationId || this.integration.id
        )
      );
    }
  }
}
