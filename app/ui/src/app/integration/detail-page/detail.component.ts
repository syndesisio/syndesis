import {
  ApplicationRef,
  Component,
  OnInit,
  OnDestroy,
} from '@angular/core';

import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { IntegrationStore, StepStore } from '@syndesis/ui/store';
import { IntegrationSupportService } from '../integration-support.service';
import { Connection, Action } from '@syndesis/ui/model';
import { Integration, Step } from '@syndesis/ui/integration';
import { IntegrationViewBase } from '../components';
import { NotificationType } from 'patternfly-ng';
import { ModalService, NotificationService } from '@syndesis/ui/common';
import { ConfigService } from '@syndesis/ui/config.service';

@Component({
  selector: 'syndesis-integration-detail-page',
  templateUrl: 'detail.component.html',
  styleUrls: ['detail.component.scss']
})
export class IntegrationDetailComponent extends IntegrationViewBase
  implements OnInit, OnDestroy {
  integration$: Observable<Integration>;
  integrationSubscription: Subscription;
  integration: Integration;
  readonly loading$: Observable<boolean>;
  routeSubscription: Subscription;
  loggingEnabled = false;

  constructor(
    public store: IntegrationStore,
    public stepStore: StepStore,
    public route: ActivatedRoute,
    public router: Router,
    public notificationService: NotificationService,
    public modalService: ModalService,
    public application: ApplicationRef,
    public integrationSupportService: IntegrationSupportService,
    private config: ConfigService
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

  validateName(name: string) {
    return name && name.length > 0 ? null : 'Name is required';
  }

  ngOnInit() {
    this.loggingEnabled = this.config.getSettings('features', 'logging', false);
    this.integrationSubscription = this.integration$.subscribe(i => {
      this.integration = i;
    });
    this.routeSubscription = this.route.paramMap
      .first( params => params.has('integrationId'))
      .subscribe(paramMap => this.store.load(paramMap.get('integrationId')));
  }

  ngOnDestroy() {
    this.integrationSubscription.unsubscribe();
    this.routeSubscription.unsubscribe();
  }

  exportIntegration() {
    super.requestAction('export', this.integration);
  }
}
