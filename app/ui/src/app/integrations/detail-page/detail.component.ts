import {
  ApplicationRef,
  Component,
  OnInit,
  OnDestroy,
} from '@angular/core';

import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { StepStore } from '../../store/step/step.store';
import { Integration, Step, Connection, Action } from '../../model';
import { IntegrationStore } from '../../store/integration/integration.store';
import { IntegrationViewBase } from '../components/integrationViewBase.component';
import { ModalService } from '../../common/modal/modal.service';
import { IntegrationSupportService } from '../../store/integration-support.service';
import { NotificationType } from 'patternfly-ng';
import { NotificationService } from 'app/common/ui-patternfly/notification-service';

@Component({
  selector: 'syndesis-integration-detail-page',
  templateUrl: 'detail.component.html',
  styleUrls: ['detail.component.scss']
})
export class IntegrationsDetailComponent extends IntegrationViewBase
  implements OnInit, OnDestroy {
  integration: Observable<Integration>;
  integrationSubscription: Subscription;
  i: Integration;
  readonly loading: Observable<boolean>;
  routeSubscription: Subscription;
  history = undefined;
  tableTestData = [
    {
      version: 'Draft',
      startTime: new Date(),
      runLength: '',
      uses: undefined,
      status: undefined,
      actions: [
        {
          label: 'Edit Draft'
        }
      ]
    },
    {
      version: 'V. 1.4',
      startTime: new Date(),
      runLength: 8,
      uses: 10,
      status: [
        {
          icon: 'pf-icon pficon-ok',
          class: '',
          label: 'Success'
        },
        {
          icon: undefined,
          class: 'label label-info pull-right',
          label: 'Running'
        }
      ],
      actions: [
        {
          label: 'Duplicate'
        }
      ]
    },
    {
      version: 'V. 1.3',
      startTime: new Date(),
      runLength: 12,
      uses: 23,
      status: [
        {
          icon: 'pf-icon pficon-ok',
          label: 'Success'
        }
      ],
      actions: [
        {
          label: 'Deploy'
        },
        {
          label: 'Duplicate'
        }
      ]
    },
    {
      version: 'V. 1.2',
      startTime: new Date(),
      runLength: 5,
      uses: 7,
      status: [
        {
          icon: 'pf-icon pficon-ok',
          label: 'Success'
        }
      ],
      actions: [
        {
          label: 'Deploy'
        },
        {
          label: 'Duplicate'
        }
      ]
    },
    {
      version: 'V. 1.1',
      startTime: new Date(),
      runLength: 22,
      uses: 3,
      status: [
        {
          icon: 'pf-icon pficon-error-circle-o',
          label: 'Failure'
        }
      ],
      actions: [
        {
          label: 'Deploy'
        },
        {
          label: 'Duplicate'
        }
      ]
    }
  ];

  constructor(
    public store: IntegrationStore,
    public stepStore: StepStore,
    public route: ActivatedRoute,
    public router: Router,
    public notificationService: NotificationService,
    public modalService: ModalService,
    public application: ApplicationRef,
    integrationSupportService: IntegrationSupportService
  ) {
    super(store, route, router, notificationService, modalService, application, integrationSupportService);
    this.integration = this.store.resource;
    this.loading = this.store.loading;
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
    if (index === this.i.steps.length - 1) {
      return 'finish';
    }
    return '';
  }

  deleteAction(integration: Integration) {
    return super
      .deleteAction(integration)
      .then(_ => this.router.navigate(['/integrations']));
  }

  onNameUpdated(name: string) {
    this.i.name = name;
    this.store
      .update(this.i)
      .toPromise()
      .then((update: Integration) => {
        this.notificationService.popNotification({
          type: NotificationType.SUCCESS,
          header: 'Update Successful',
          message: 'Updated description'
        });
      })
      .catch(reason => {
        this.notificationService.popNotification({
          type: NotificationType.WARNING,
          header: 'Update Failed',
          message: `Failed to update description: ${reason}`
        });
      });
  }

  onAttributeUpdated(attr: string, value: string) {
    this.i[attr] = value;
    this.store
      .update(this.i)
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

  duplicateRevision(revision) {
    const integration = JSON.parse(JSON.stringify(this.i));
    delete integration.id;
    // update these fields
    integration.name = integration.name + ' (copy)';
    integration.steps = revision.spec.steps;
    // initialize these fields
    integration.desiredStatus = 'Draft';
    integration.currentStatus = undefined;
    integration.createdDate = undefined;
    integration.lastUpdated = undefined;
    integration.revisions = [];
    this.notificationService.popNotification({
      type: NotificationType.INFO,
      header: 'Duplicating revision',
      message: `Duplicating revision ${revision.version}`
    });
    const sub = this.store.create(integration).subscribe(
      created => {
        this.router.navigate(['/integrations', created.id]);
        sub.unsubscribe();
      },
      resp => {
        this.notificationService.popNotification({
          type: NotificationType.DANGER,
          header: 'Failed to duplicate revision',
          message:
            resp.length !== undefined ? resp.data[0].message : resp.data.message
        });
        sub.unsubscribe();
      }
    );
  }

  deployRevision(revision) {
    const integration = JSON.parse(JSON.stringify(this.i));
    integration.steps = revision.spec.steps;
    this.notificationService.popNotification({
      type: NotificationType.INFO,
      header: 'Deploying revision',
      message: `Deploying revision ${revision.version}`
    });
    const sub = this.store.update(integration).subscribe(
      updated => {
        this.notificationService.popNotification({
          type: NotificationType.SUCCESS,
          header: 'Deployment successful',
          message: `Deployed revision ${revision.version}`
        });
        sub.unsubscribe();
      },
      resp => {
        this.notificationService.popNotification({
          type: NotificationType.DANGER,
          header: 'Failed to deploy revision',
          message:
            resp.length !== undefined ? resp.data[0].message : resp.data.message
        });
        sub.unsubscribe();
      }
    );
  }

  onRevisionAction(action: any, revision): void {
    switch (action.action) {
      case 'duplicate':
        this.duplicateRevision(revision);
        break;
      case 'deploy':
        this.deployRevision(revision);
        break;
      default:
        break;
    }
  }

  validateName(name: string) {
    return name && name.length > 0 ? null : 'Name is required';
  }

  ngOnInit() {
    this.integrationSubscription = this.integration.subscribe(
      (i: Integration) => {
        if (!i || !i.id) {
          return;
        }
        this.i = i;
        this.history = [];
        if (i.revisions) {
          this.history = i.revisions
            .sort((a, b) => {
              return b.version - a.version;
            })
            .map(rev => {
              const status = {
                icon: undefined,
                class: '',
                label: ''
              };
              // TODO this is kinda fake data
              switch (rev.currentState) {
                case 'Draft':
                case 'Pending':
                case 'Inactive':
                case 'Undeployed':
                  break;
                case 'Active':
                  (status.icon = 'pf-icon pficon-ok'),
                    (status.label = 'Success');
                  break;
                case 'Error':
                  status.icon = 'pf-icon pficon-error-circle-o';
                  status.label = 'Failure';
                  break;
                default:
                  break;
              }
              const row = {
                revision: rev,
                version: rev.version,
                // TODO this is totally fake data
                startTime: Date.parse(rev['startTime'] || '2017/9/15'),
                uses: rev['uses'] || Math.floor(Math.random() * 10),
                runLength: rev['runLength'] || Math.floor(Math.random() * 300),
                status: [status],
                actions: [
                  /*
                  {
                    label: 'Duplicate',
                    action: 'duplicate',
                  },
                  */
                ]
              };
              let isDeployed = false;
              if (row.version === i.deployedRevisionId) {
                isDeployed = true;
                const state = {
                  icon: undefined,
                  class: '',
                  label: rev.currentState
                };
                switch (rev.currentState) {
                  case 'Draft':
                  case 'Pending':
                  case 'Inactive':
                  case 'Undeployed':
                    state.class = 'label label-info pull-right';
                    break;
                  case 'Active':
                    state.class = 'label label-success pull-right';
                    break;
                  case 'Error':
                    state.class = 'label label-warning pull-right';
                    break;
                  default:
                    break;
                }
                row.status.push(state);
              }
              if (!isDeployed && rev.spec && rev.spec.steps) {
                row.actions.push({
                  label: 'Deploy',
                  action: 'deploy'
                });
              }
              return row;
            });
        }
      }
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

  exportIntegration() {
    super.requestAction('export', this.i);
  }
}
