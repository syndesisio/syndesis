import { switchMap } from 'rxjs/operators';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';
import { combineLatest, Observable, Subscription, of } from 'rxjs';

import { NavigationService } from '@syndesis/ui/common';
import { Integration } from '@syndesis/ui/platform';
import { CurrentFlowService } from '@syndesis/ui/integration/edit-page/current-flow.service';
import { FlowPageService } from '@syndesis/ui/integration/edit-page/flow-page.service';

@Component({
  selector: 'syndesis-integration-edit-page',
  templateUrl: './edit-page.component.html',
  styleUrls: ['./edit-page.component.scss']
})
export class IntegrationEditPage implements OnInit, OnDestroy {
  integration: Observable<Integration>;
  readonly loading: Observable<boolean>;

  routeSubscription: Subscription;
  urls: UrlSegment[];
  _canContinue = false;
  position: number;

  constructor(
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router,
    public navigationService: NavigationService
  ) {}

  getPageContainer() {
    switch (this.flowPageService.getCurrentStepKind(this.route)) {
      case 'mapper':
        return 'mapper-main';
      default:
        return 'wizard-main';
    }
  }

  ngOnInit() {
    this.routeSubscription = combineLatest(this.route.paramMap, this.route.data)
      .pipe(
        switchMap(([params, data]) =>
          of({
            integrationId: params.get('integrationId'),
            flowId: params.get('flowId'),
            integration: data.integration
          })
        )
      )
      .subscribe(({ integrationId, flowId, integration }) => {
        this.currentFlowService.flowId = flowId;
        this.currentFlowService.integration = integration;
        // If the current flow ID isn't in the param map, redirect so it's available
        if (integrationId && !flowId && this.currentFlowService.flowId) {
          this.router.navigate(['..', this.currentFlowService.flowId, 'edit'], {
            relativeTo: this.route
          });
          return;
        }
        // Check and see if we're just at the root of the editor, if so validate
        // the current flow
        const currentChildPage = this.flowPageService.getCurrentChild(
          this.route
        );
        if (!currentChildPage) {
          this.router.navigate(['save-or-add-step'], {
            queryParams: { validate: true },
            relativeTo: this.route
          });
        }
      });
    this.navigationService.hide();
  }

  ngOnDestroy() {
    this.currentFlowService.cleanup();
    this.navigationService.show();
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
  }
}
