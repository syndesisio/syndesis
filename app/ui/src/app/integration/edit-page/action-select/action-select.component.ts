import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Subscription } from 'rxjs/Subscription';

import { Actions, Action, Connector, UserService, Step } from '@syndesis/ui/platform';
import { log, getCategory } from '@syndesis/ui/logging';
import { CurrentFlow, FlowEvent, FlowPage } from '@syndesis/ui/integration/edit-page';
import { ConnectorStore } from '@syndesis/ui/store';

const category = getCategory('Integrations');

@Component({
  selector: 'syndesis-integration-action-select',
  templateUrl: 'action-select.component.html',
  styleUrls: ['./action-select.component.scss']
})
export class IntegrationSelectActionComponent extends FlowPage
  implements OnInit, OnDestroy {
  actions: Observable<Actions> = Observable.empty();
  filteredActions: Subject<Actions> = new BehaviorSubject(<Actions>{});
  connector: Observable<Connector>;
  loading: Observable<boolean>;
  routeSubscription: Subscription;
  actionsSubscription: Subscription;
  position: number;
  step: Step;
  currentStep: number;

  constructor(
    public connectorStore: ConnectorStore,
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    private userService: UserService
  ) {
    super(currentFlow, route, router);
    this.connector = connectorStore.resource;
    this.loading = connectorStore.loading;
    connectorStore.clear();
  }

  onSelected(action: Action) {
    log.debugc(() => 'Selected action: ' + action.name, category);
    this.currentFlow.events.emit({
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
    super.goBack([ 'connection-select', this.position ]);
  }

  loadActions() {
    if (!this.currentFlow.loaded) {
      return;
    }
    const step = (this.step = this.currentFlow.getStep(this.position));
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

    if (this.currentStep === this.currentFlow.getFirstPosition()) {
      this.actions = this.connector
        .filter(connector => connector !== undefined)
        .switchMap(connector => [connector.actions.filter(action => action.pattern === 'From')]);
    }

    if (this.currentStep > this.currentFlow.getFirstPosition()
      && this.currentStep <= this.currentFlow.getLastPosition()) {
      this.actions = this.connector
        .filter(connector => connector !== undefined)
        .switchMap(connector => [connector.actions.filter(action => action.pattern === 'To')]);
    }

    this.actionsSubscription = this.actions.subscribe(_ =>
      this.currentFlow.events.emit({
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
    super.ngOnDestroy();
    if (this.actionsSubscription) {
      this.actionsSubscription.unsubscribe();
    }
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
  }
}
