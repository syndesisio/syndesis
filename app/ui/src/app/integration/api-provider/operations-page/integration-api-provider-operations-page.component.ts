import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription, combineLatest, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import {
  CurrentFlowService,
  FlowPageService
} from '@syndesis/ui/integration/edit-page';
import { NavigationService } from '@syndesis/ui/common';

@Component({
  selector: 'syndesis-integration-api-provider-operations',
  templateUrl: './integration-api-provider-operations-page.component.html',
  styleUrls: [
    '../../integration-common.scss',
    './integration-api-provider-operations-page.component.scss'
  ]
})
export class ApiProviderOperationsComponent implements OnInit, OnDestroy {
  routeSubscription: Subscription;

  constructor(
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router,
    public navigationService: NavigationService
  ) {}

  ngOnInit() {
    this.routeSubscription = combineLatest(this.route.paramMap, this.route.data)
      .pipe(
        switchMap(([params, data]) => {
          return of({
            flowId: params.get('flowId'),
            integration: data.integration
          });
        })
      )
      .subscribe(({ flowId, integration }) => {
        this.currentFlowService.flowId = flowId;
        this.currentFlowService.integration = integration;
      });
    this.navigationService.hide();
  }

  ngOnDestroy() {
    this.navigationService.show();
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
  }
}
