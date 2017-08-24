import {
  Component,
  EventEmitter,
  Input,
  OnInit,
  OnDestroy,
  Output,
  ChangeDetectorRef,
} from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';

import { Actions, Action } from '../../../model';
import { log, getCategory } from '../../../logging';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { ConnectorStore } from '../../../store/connector/connector.store';
import { Step, Connector } from '../../../model';
import { FlowPage } from '../flow-page';
import { ObjectPropertyFilterConfig } from '../../../common/object-property-filter.pipe';

const category = getCategory('Integrations');

@Component({
  selector: 'syndesis-integrations-action-select',
  templateUrl: 'action-select.component.html',
  styleUrls: ['./action-select.component.scss'],
})
export class IntegrationsSelectActionComponent extends FlowPage
  implements OnInit, OnDestroy {
  actions: Actions = [];
  connector: Observable<Connector>;
  loading: Observable<boolean>;
  routeSubscription: Subscription;
  connectorSubscription: Subscription;
  position: number;
  step: Step;

  @Input()
  filter: ObjectPropertyFilterConfig = {
    filter: '',
    propertyName: 'name',
  };
  @Output() filterChange = new EventEmitter<ObjectPropertyFilterConfig>();

  constructor(
    public connectorStore: ConnectorStore,
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public detector: ChangeDetectorRef,
  ) {
    super(currentFlow, route, router, detector);
    this.connector = connectorStore.resource;
    this.loading = connectorStore.loading;
  }

  onSelected(action: Action) {
    log.debugc(() => 'Selected action: ' + action.name, category);
    this.currentFlow.events.emit({
      kind: 'integration-set-action',
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
    super.goBack(['connection-select', this.position]);
  }

  ngOnInit() {
    this.connectorSubscription = this.connector.subscribe(
      (connector: Connector) => {
        this.actions = connector.actions;
        this.currentFlow.events.emit({
          kind: 'integration-action-select',
          position: this.position,
        });
      },
    );
    this.routeSubscription = this.route.params
      .pluck<Params, string>('position')
      .map((position: string) => {
        this.position = Number.parseInt(position);
        const step = this.step = this.currentFlow.getStep(this.position);
        if (step && step.connection) {
          this.connectorStore.load(step.connection.connectorId);
        } else {
          this.router.navigate(['connection-select', this.position], {
            relativeTo: this.route.parent,
          });
          return;
        }
      })
      .subscribe();
  }

  ngOnDestroy() {
    if (this.flowSubscription) {
      this.flowSubscription.unsubscribe();
    }
    if (this.connectorSubscription) {
      this.connectorSubscription.unsubscribe();
    }
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
  }

  filterInputChange(value: string) {
    this.filter.filter = value;
    this.filterChange.emit(this.filter);
  }
}
