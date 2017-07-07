import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';

import { log, getCategory } from '../../../logging';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { ConnectionStore } from '../../../store/connection/connection.store';
import { Connections, Connection } from '../../../model';
import { FlowPage } from '../flow-page';
import { ObjectPropertyFilterConfig } from '../../../common/object-property-filter.pipe';
import { ObjectPropertySortConfig } from '../../../common/object-property-sort.pipe';

const category = getCategory('Integrations');

@Component({
  moduleId: module.id,
  selector: 'syndesis-integrations-connection-select',
  templateUrl: 'connection-select.component.html',
  styleUrls: ['./connection-select.component.scss'],
})
export class IntegrationsSelectConnectionComponent extends FlowPage
  implements OnInit, OnDestroy {
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
  routeSubscription: Subscription;
  position: number;

  constructor(
    public store: ConnectionStore,
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public detector: ChangeDetectorRef,
  ) {
    super(currentFlow, route, router, detector);
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
        this.router.navigate(['action-select', this.position], {
          relativeTo: this.route.parent,
        });
      },
    });
  }

  goBack() {
    const step = this.currentFlow.getStep(this.position);
    step.connection = undefined;
    super.goBack(['save-or-add-step']);
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

  ngOnInit() {
    this.routeSubscription = this.route.params
      .pluck<Params, string>('position')
      .map((position: string) => {
        this.position = Number.parseInt(position);
        this.currentFlow.events.emit({
          kind: 'integration-connection-select',
          position: this.position,
        });
      })
      .subscribe();
    /*
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
    */
    this.store.loadAll();
  }

  ngOnDestroy() {
    this.routeSubscription.unsubscribe();
  }
}
