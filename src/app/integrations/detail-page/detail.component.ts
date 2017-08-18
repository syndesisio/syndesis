import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { StepStore } from '../../store/step/step.store';
import { Integration } from '../../model';
import { IntegrationStore } from '../../store/integration/integration.store';
import { IntegrationViewBase } from '../components/integrationViewBase.component';
import { NotificationService } from 'patternfly-ng';

@Component({
  selector: 'syndesis-integration-detail-page',
  templateUrl: 'detail.component.html',
  styleUrls: ['detail.component.scss'],
})
export class IntegrationsDetailComponent extends IntegrationViewBase implements OnInit, OnDestroy {
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
          label: 'Edit Draft',
        },
      ],
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
          label: 'Success',
        },
        {
          icon: undefined,
          class: 'label label-info pull-right',
          label: 'Running',
        },
      ],
      actions: [
        {
          label: 'Duplicate',
        },
      ],
    },
    {
      version: 'V. 1.3',
      startTime: new Date(),
      runLength: 12,
      uses: 23,
      status: [
        {
          icon: 'pf-icon pficon-ok',
          label: 'Success',
        },
      ],
      actions: [
        {
          label: 'Deploy',
        },
        {
          label: 'Duplicate',
        },
      ],
    },
    {
      version: 'V. 1.2',
      startTime: new Date(),
      runLength: 5,
      uses: 7,
      status: [
        {
          icon: 'pf-icon pficon-ok',
          label: 'Success',
        },
      ],
      actions: [
        {
          label: 'Deploy',
        },
        {
          label: 'Duplicate',
        },
      ],
    },
    {
      version: 'V. 1.1',
      startTime: new Date(),
      runLength: 22,
      uses: 3,
      status: [
        {
          icon: 'pf-icon pficon-error-circle-o',
          label: 'Failure',
        },
      ],
      actions: [
        {
          label: 'Deploy',
        },
        {
          label: 'Duplicate',
        },
      ],
    },
  ];

  constructor(
    public store: IntegrationStore,
    public stepStore: StepStore,
    public route: ActivatedRoute,
    public router: Router,
    public detector: ChangeDetectorRef,
    public notificationService: NotificationService,
  ) {
    super(store, route, router, notificationService);
    this.integration = this.store.resource;
    this.loading = this.store.loading;
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

  deleteAction(i: Integration, success?: (i: Integration) => void, error?: (reason: any) => void ) {
    super.deleteAction(i, (i: Integration) => {
      this.router.navigate(['/integrations']);
    }, error);
  }

  ngOnInit() {
    this.integrationSubscription = this.integration.subscribe(
      (i: Integration) => {
        this.i = i;
      },
    );
    this.routeSubscription = this.route.params
      .pluck<Params, string>('integrationId')
      .map((id: string) => this.store.load(id))
      .subscribe();
    setTimeout(() => {
      this.history = JSON.parse(JSON.stringify(this.tableTestData));
      this.detector.detectChanges();
    }, 1000);
  }

  ngOnDestroy() {
    this.integrationSubscription.unsubscribe();
    this.routeSubscription.unsubscribe();
  }
}
