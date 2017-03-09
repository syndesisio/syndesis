import { Component, EventEmitter, Input, OnInit, OnDestroy, Output, ChangeDetectorRef } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';

import { log, getCategory } from '../../../logging';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { ConnectorStore } from '../../../store/connector/connector.store';
import { Actions, Action } from '../../../model';
import { Connector } from '../../../model';
import { ObjectPropertyFilterConfig } from '../../../common/object-property-filter.pipe';

const category = getCategory('Integrations');

@Component({
  selector: 'ipaas-integrations-action-select',
  templateUrl: 'action-select.component.html',
  styleUrls: [ './action-select.component.scss'],
})
export class IntegrationsSelectActionComponent implements OnInit, OnDestroy {

  actions: Actions;
  connector: Observable<Connector>;
  loading: Observable<boolean>;
  flowSubscription: Subscription;
  routeSubscription: Subscription;
  position: number;


  @Input()
  filter: ObjectPropertyFilterConfig = {
    filter: '',
    propertyName: 'name',
  };
  @Output()
  filterChange = new EventEmitter<ObjectPropertyFilterConfig>();


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
      kind: 'integration-set-action',
      position: this.position,
      action: action,
      onSave: () => {
        this.router.navigate(['action-configure', this.position], { relativeTo: this.route.parent });
      },
    });
  }

  /*
  atEnd() {
    if (this.currentFlow.isEmpty()) {
      // if it's empty, we're always at the start action step
      return false;
    }
    return this.currentFlow.atEnd(this.position);
  }
  */

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case 'integration-no-action':
        break;
    }
  }

  cancel() {
    this.router.navigate(['integrations']);
  }

  goBack() {
    this.router.navigate(['connection-select', this.position], { relativeTo: this.route.parent });
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
        if (step && step.connection) {
          this.connectorStore.load(step.connection.connectorId);
        } else {
          this.router.navigate(['connection-select', this.position], { relativeTo: this.route.parent });
          return;
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

  filterInputChange(value: string) {
    this.filter.filter = value;
    this.filterChange.emit(this.filter);
  }

}
