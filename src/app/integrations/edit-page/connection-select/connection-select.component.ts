import { Component, OnInit, OnDestroy } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';

import { log, getCategory } from '../../../logging';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { ConnectionStore } from '../../../store/connection/connection.store';
import { Connections, Connection } from '../../../model';
import { ObjectPropertyFilterConfig } from '../../../common/object-property-filter.pipe';
import { ObjectPropertySortConfig } from '../../../common/object-property-sort.pipe';

const category = getCategory('Integrations');

@Component({
  moduleId: module.id,
  selector: 'ipaas-integrations-connection-select',
  templateUrl: 'connection-select.component.html',
})
export class IntegrationsSelectConnectionComponent implements OnInit, OnDestroy {

  connections: Observable<Connections>;
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
    private store: ConnectionStore,
    private currentFlow: CurrentFlow,
    private route: ActivatedRoute,
    private router: Router,
    ) {
    this.loading = store.loading;
    this.connections = store.list;
  }

  onSelected(connection: Connection) {
    log.debugc(() => 'Selected connection: ' + connection.name, category);
    this.currentFlow.events.emit({
      kind: 'integration-set-connection',
      position: this.position,
      connection: connection,
      onSave: () => {
        this.router.navigate(['action-select', this.position], { relativeTo: this.route.parent });
      },
    });
  }

  cancel() {
    this.router.navigate(['integrations']);
  }

  goBack() {
    this.router.navigate(['save-or-add-step', 'new'], { relativeTo: this.route.parent });
  }

  positionText() {
    if (this.position === 0) {
      return 'start';
    }
    if (this.position === this.currentFlow.getLastPosition()) {
      return 'end';
    }
    return '';
  }

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case 'integration-no-connection':
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
          kind: 'integration-connection-select',
          position: this.position,
        });
      })
      .subscribe();
    this.connections.map((connections: Connections) => {
      const config = this.currentFlow.getStep(this.position);
      if (config) {
        const id = config.id;
        for (const connection of connections) {
          if (connection.id === id) {
            log.debugc(() => 'Found connection: ' + connection.name, category);
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
