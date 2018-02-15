import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Subscription } from 'rxjs/Subscription';

import { Actions, Action, Connector, UserService, Step, IntegrationSupportService } from '@syndesis/ui/platform';
import { log, getCategory } from '@syndesis/ui/logging';
import { CurrentFlowService, FlowEvent, FlowPageService } from '@syndesis/ui/integration/edit-page';
import { ConnectorStore } from '@syndesis/ui/store';

const category = getCategory('Integrations');

@Component({
  selector: 'syndesis-integration-action-select',
  templateUrl: 'action-select.component.html',
  styleUrls: ['./action-select.component.scss']
})
export class IntegrationSelectActionComponent implements OnInit, OnDestroy {
  flowSubscription: Subscription;
  actions$: Observable<Actions> = Observable.empty();
  filteredActions$: Subject<Actions> = new BehaviorSubject(<Actions>{});
  connector$: Observable<Connector>;
  loading$: Observable<boolean>;
  routeSubscription: Subscription;
  actionsSubscription: Subscription;
  position: number;
  step: Step;
  currentStep: number;

  constructor(
    public connectorStore: ConnectorStore,
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router,
    private userService: UserService,
    private integrationSupportService: IntegrationSupportService,
  ) {
    this.flowSubscription = currentFlowService.events.subscribe(
      (event: FlowEvent) => {
        this.handleFlowEvent(event);
      }
    );
    this.connector$ = connectorStore.resource;
    this.loading$ = connectorStore.loading;
    connectorStore.clear();
  }

  onSelected(action: Action) {
    log.debugc(() => 'Selected action: ' + action.name, category);
    this.currentFlowService.events.emit({
      kind: 'integration-set-action',
      position: this.position,
      action: action,
      onSave: () => {
        this.router.navigate(['action-configure', this.position], {
          relativeTo: this.route.parent
        });
      }
    });
  }

  goBack() {
    this.flowPageService.goBack([ 'connection-select', this.position ], this.route);
  }

  loadActions() {
    if (!this.currentFlowService.loaded) {
      return;
    }
    const step = (this.step = this.currentFlowService.getStep(this.position));
    if (!step) {
      /* Safety net */
      this.router.navigate(['save-or-add-step'], {
        relativeTo: this.route.parent
      });
      return;
    }
    if (!step.connection) {
      this.router.navigate(['connection-select', this.position], {
        relativeTo: this.route.parent
      });
      return;
    }
    if (step.action) {
      this.router.navigate(['action-configure', this.position], {
        relativeTo: this.route.parent
      });
      return;
    }
    this.connectorStore.load(step.connection.connectorId);
  }

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case 'integration-updated':
        this.loadActions();
        break;
      default:
        break;
    }
  }

  ngOnInit() {
    this.currentStep = +this.route.snapshot.paramMap.get('position');

    if (this.currentStep === this.currentFlowService.getFirstPosition()) {
      this.actions$ = this.connector$
        .filter(connector => connector !== undefined)
        .switchMap(connector => [connector.actions.filter(action => action.pattern === 'From')]);
    }

    if (this.currentStep > this.currentFlowService.getFirstPosition()
      && this.currentStep <= this.currentFlowService.getLastPosition()) {
      this.actions$ = this.connector$
        .filter(connector => connector !== undefined)
        .switchMap(connector => [connector.actions.filter(action => action.pattern === 'To')]);
    }

    this.actionsSubscription = this.actions$.subscribe(_ =>
      this.currentFlowService.events.emit({
        kind: 'integration-action-select',
        position: this.position
      })
    );

    this.route.paramMap.subscribe(params => {
      this.position = +params.get('position');
      this.loadActions();
    });
  }

  ngOnDestroy() {
    if (this.flowSubscription) {
      this.flowSubscription.unsubscribe();
    }
    if (this.actionsSubscription) {
      this.actionsSubscription.unsubscribe();
    }
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
  }
}
