import { Component, OnInit, OnDestroy } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';

import { log, getCategory } from '../../../logging';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { ActionStore } from '../../../store/action/action.store';
import { Actions, Action } from '../../../model';
import { ObjectPropertyFilterConfig } from '../../../common/object-property-filter.pipe';
import { ObjectPropertySortConfig } from '../../../common/object-property-sort.pipe';

const category = getCategory('Integrations');

@Component({
  moduleId: module.id,
  selector: 'ipaas-integrations-select-action',
  templateUrl: 'select-action.component.html',
})
export class IntegrationsSelectActionComponent implements OnInit, OnDestroy {

  actions: Observable<Actions>;
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
    private store: ActionStore,
    private currentFlow: CurrentFlow,
    private route: ActivatedRoute,
    private router: Router,
    ) {
    this.loading = store.loading;
    this.actions = store.list;
  }

  onSelect(action: Action) {
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
    this.routeSubscription = this.route.params.pluck<Params, string>('position')
      .map((position: string) => {
        this.position = Number.parseInt(position);
        this.currentFlow.events.emit({
          kind: 'integration-action-select',
          position: this.position,
        });
      })
      .subscribe();
    this.actions.map((actions: Actions) => {
      const config = this.currentFlow.getStep(this.position);
      if (config) {
        const id = config.id;
        for (const action of actions) {
          if (action.id === id) {
            log.debugc(() => 'Found action: ' + action.name, category);
          }
        }
      }
    });
    this.store.loadAll();
  }

  ngOnDestroy() {
    this.routeSubscription.unsubscribe();
    this.flowSubscription.unsubscribe();
  }

}
