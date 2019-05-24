import { filter, switchMap } from 'rxjs/operators';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  Observable,
  EMPTY,
  Subject,
  BehaviorSubject,
  Subscription,
} from 'rxjs';

import { Actions, Action, Connector, Step } from '@syndesis/ui/platform';
import {
  CurrentFlowService,
  FlowEvent,
  FlowPageService,
  INTEGRATION_UPDATED,
  INTEGRATION_SET_STEP,
  INTEGRATION_SET_ACTION,
  INTEGRATION_CANCEL_CLICKED,
} from '@syndesis/ui/integration/edit-page';
import { ConnectorStore } from '@syndesis/ui/store';

@Component({
  selector: 'syndesis-integration-action-select',
  templateUrl: 'action-select.component.html',
  styleUrls: [
    '../../integration-common.scss',
    './action-select.component.scss',
  ],
})
export class IntegrationSelectActionComponent implements OnInit, OnDestroy {
  flowSubscription: Subscription;
  actions$: Observable<Actions> = EMPTY;
  filteredActions$: Subject<Actions> = new BehaviorSubject(<Actions>{});
  connector$: Observable<Connector>;
  loading$: Observable<boolean>;
  routeSubscription: Subscription;
  position: number;
  step: Step;

  constructor(
    public connectorStore: ConnectorStore,
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router
  ) {
    this.connector$ = connectorStore.resource;
    this.loading$ = connectorStore.loading;
    connectorStore.clear();
  }

  onSelected(action: Action) {
    this.currentFlowService.events.emit({
      kind: INTEGRATION_SET_ACTION,
      position: this.position,
      action: action,
      onSave: () => {
        this.router.navigate(['action-configure', this.position], {
          relativeTo: this.route.parent,
        });
      },
    });
  }

  goBack() {
    const step = this.currentFlowService.getStep(this.position);
    step.stepKind = undefined;
    this.currentFlowService.events.emit({
      kind: INTEGRATION_SET_STEP,
      position: this.position,
      step: step,
      onSave: () => {
        this.flowPageService.goBack(['step-select', this.position], this.route);
      },
    });
  }

  loadActions() {
    if (!this.currentFlowService.loaded) {
      return;
    }
    // filter the avaliable connections based on where we are in the flow
    if (this.position === this.currentFlowService.getFirstPosition()) {
      this.actions$ = this.connector$.pipe(
        filter(connector => connector !== undefined),
        switchMap(connector => [
          connector.actions.filter(action => action.pattern === 'From'),
        ])
      );
    }
    if (
      this.position > this.currentFlowService.getFirstPosition() &&
      this.position <= this.currentFlowService.getLastPosition()
    ) {
      this.actions$ = this.connector$.pipe(
        filter(connector => connector !== undefined),
        switchMap(connector => [
          connector.actions.filter(action => action.pattern === 'To'),
        ])
      );
    }
    if (
      this.position > this.currentFlowService.getFirstPosition() &&
      this.position < this.currentFlowService.getLastPosition()
    ) {
      this.actions$ = this.connector$.pipe(
        filter(connector => connector !== undefined),
        switchMap(connector => [
          connector.actions.filter(
            action => action.pattern === 'To' || action.pattern === 'Pipe'
          ),
        ])
      );
    }
    const step = (this.step = this.currentFlowService.getStep(this.position));
    if (!step) {
      /* Safety net */
      this.router.navigate(['save-or-add-step'], {
        relativeTo: this.route.parent,
      });
      return;
    }
    if (!step.connection) {
      this.router.navigate(['step-select', this.position], {
        relativeTo: this.route.parent,
      });
      return;
    }
    if (step.action) {
      this.router.navigate(['action-configure', this.position], {
        relativeTo: this.route.parent,
      });
      return;
    }
    this.connectorStore.load(step.connection.connectorId);
  }

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case INTEGRATION_UPDATED:
        this.loadActions();
        break;
      case INTEGRATION_CANCEL_CLICKED:
        this.flowPageService.maybeRemoveStep(
          this.router,
          this.route,
          this.position
        );
        break;
      default:
        break;
    }
  }

  ngOnInit() {
    this.flowSubscription = this.currentFlowService.events.subscribe(
      (event: FlowEvent) => {
        this.handleFlowEvent(event);
      }
    );
    this.route.paramMap.subscribe(params => {
      this.position = +params.get('position');
      this.loadActions();
    });
    this.connector$.subscribe(connector => {
      if (connector && connector.id === 'api-provider') {
        this.router.navigate(['api-provider', 'create'], {
          relativeTo: this.route.parent,
        });
      }
    });
  }

  ngOnDestroy() {
    if (this.flowSubscription) {
      this.flowSubscription.unsubscribe();
    }
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
  }
}
