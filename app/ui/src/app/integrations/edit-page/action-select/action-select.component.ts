import { Component, OnInit, OnDestroy } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';

import { Actions, Action, Step, Connector } from '../../../model';
import { log, getCategory } from '../../../logging';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { ConnectorStore } from '../../../store/connector/connector.store';
import { FlowPage } from '../flow-page';

import { TourService } from 'ngx-tour-ngx-bootstrap';
import { UserService } from '../../../common/user.service';

const category = getCategory('Integrations');

@Component({
  selector: 'syndesis-integrations-action-select',
  templateUrl: 'action-select.component.html',
  styleUrls: ['./action-select.component.scss']
})
export class IntegrationsSelectActionComponent extends FlowPage
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
    public tourService: TourService,
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

    /**
     * If guided tour state is set to be shown (i.e. true), then show it for this page, otherwise don't.
     */
    if (this.userService.getTourState() === true) {
      this.tourService.initialize([ {
        anchorId: 'actions.available',
        title: 'Available Actions',
        content: 'When an integration uses the selected connection it performs the action you select.',
        placement: 'top',
        } ],
      );
      setTimeout(() => this.tourService.start());
    }
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
