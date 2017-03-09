import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';

import { log, getCategory } from '../../../logging';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { ActionStore } from '../../../store/action/action.store';
import { ConnectorStore } from '../../../store/connector/connector.store';
import { Actions, Action } from '../../../model';
import { Connector, Connectors } from '../../../model';
import { ObjectPropertyFilterConfig } from '../../../common/object-property-filter.pipe';
import { ObjectPropertySortConfig } from '../../../common/object-property-sort.pipe';

const category = getCategory('Integrations');

@Component({
  selector: 'ipaas-integrations-action-select',
  templateUrl: 'action-select.component.html',
})
export class IntegrationsSelectActionComponent implements OnInit, OnDestroy {

  actions: Actions;
  connector: Observable<Connector>;
  loading: Observable<boolean>;
  filter: ObjectPropertyFilterConfig = {
    filter: '',
    propertyName: 'name',
  };
  sort: ObjectPropertySortConfig = {
    sortField: 'name',
    descending: false,
  };
  flowSubscription: Subscription;
  routeSubscription: Subscription;
  position: number;

  constructor(
    private connectorStore: ConnectorStore,
    private currentFlow: CurrentFlow,
    private route: ActivatedRoute,
    private router: Router,
    private changeDetectorRef: ChangeDetectorRef,
    ) {
      this.connector = connectorStore.resource;
      this.loading = connectorStore.loading;
  }

  onSelected(action: Action) {
    log.debugc(() => 'Selected action: ' + action.name, category);
    this.currentFlow.events.emit({
      kind: 'integration-selected-action',
      position: this.position,
      action: action,
    });
  }

  atEnd() {
    if (this.currentFlow.isEmpty()) {
      // if it's empty, we're always at the start action step
      return false;
    }
    return this.currentFlow.atEnd(this.position);
  }

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case 'integration-no-action':
        break;
    }
  }

  ngOnInit() {
    this.flowSubscription = this.currentFlow.events.subscribe((event: FlowEvent) => {
      this.handleFlowEvent(event);
    });
    this.connector.subscribe((connector: Connector) => {
      this.actions = connector.actions;
      // TODO oh no, why is this needed...
      setTimeout(() => {
        this.changeDetectorRef.detectChanges();
      }, 10);
    });
    this.routeSubscription = this.route.params.pluck<Params, string>('position')
      .map((position: string) => {
        this.position = Number.parseInt(position);
        const step = this.currentFlow.getStep(this.position);
        console.log('Step: ', step);
        if (step && step.connection) {
          this.connectorStore.load(step.connection.connectorId);
        }
        this.currentFlow.events.emit({
          kind: 'integration-action-select',
          position: this.position,
        });
      })
      .subscribe();
  }

  ngOnDestroy() {
    this.routeSubscription.unsubscribe();
    this.flowSubscription.unsubscribe();
  }

}
