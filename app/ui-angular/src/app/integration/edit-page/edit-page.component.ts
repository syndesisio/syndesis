import { switchMap } from 'rxjs/operators';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, RouterStateSnapshot } from '@angular/router';
import { combineLatest, Subscription, of, Observable, from } from 'rxjs';

import { NavigationService, ModalService } from '@syndesis/ui/common';
import { CurrentFlowService } from '@syndesis/ui/integration/edit-page/current-flow.service';
import { FlowPageService } from '@syndesis/ui/integration/edit-page/flow-page.service';
import { CanComponentDeactivate } from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-integration-edit-page',
  templateUrl: './edit-page.component.html',
  styleUrls: ['./edit-page.component.scss'],
})
export class IntegrationEditPage
  implements OnInit, OnDestroy, CanComponentDeactivate {
  routeSubscription: Subscription;

  constructor(
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router,
    public navigationService: NavigationService,
    private modalService: ModalService
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
            integration: data.integration,
          })
        )
      )
      .subscribe(({ integrationId, flowId, integration }) => {
        this.currentFlowService.flowId = flowId;
        this.currentFlowService.integration = integration;
        // If the current flow ID isn't in the param map, redirect so it's available
        if (integrationId && !flowId && this.currentFlowService.flowId) {
          this.router.navigate(['..', this.currentFlowService.flowId, 'edit'], {
            relativeTo: this.route,
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
            relativeTo: this.route,
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

  canDeactivate(
    nextState: RouterStateSnapshot
  ): boolean | Observable<boolean> | Promise<boolean> {
    return (
      nextState.url.indexOf(this.currentFlowService.integration.id) !== -1 ||
      nextState.url.endsWith('operations') ||
      this.currentFlowService.dirty$.pipe(
        switchMap(dirty => {
          if (dirty) {
            return from(
              this.modalService
                .show('leave-editor-prompt')
                .then(modal => modal.result)
            );
          } else {
            return of(true);
          }
        })
      )
    );
  }
}
