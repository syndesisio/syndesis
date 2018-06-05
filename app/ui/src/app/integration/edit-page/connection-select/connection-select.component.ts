import { map, first } from 'rxjs/operators';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subscription, BehaviorSubject } from 'rxjs';

import { log, getCategory } from '@syndesis/ui/logging';
import { Connections, Connection, UserService } from '@syndesis/ui/platform';
import {
  CurrentFlowService,
  FlowEvent,
  FlowPageService
} from '@syndesis/ui/integration/edit-page';
import { ConnectionStore } from '@syndesis/ui/store';

const category = getCategory('Integrations');

@Component({
  selector: 'syndesis-integration-connection-select',
  templateUrl: 'connection-select.component.html',
  styleUrls: [
    '../../integration-common.scss',
    './connection-select.component.scss'
  ]
})
export class IntegrationSelectConnectionComponent implements OnInit, OnDestroy {
  routeSubscription: Subscription;
  flowSubscription: Subscription;
  loading$: Observable<boolean>;
  connections$: Observable<Connections>;
  filteredConnections$ = new BehaviorSubject(<Connections>{});
  position: number;
  positionText: String;

  constructor(
    public store: ConnectionStore,
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router,
    private userService: UserService
  ) {
    this.flowSubscription = this.currentFlowService.events.subscribe(
      (event: FlowEvent) => {
        this.handleFlowEvent(event);
      }
    );
    this.loading$ = store.loading;
    this.connections$ = store.list.pipe(
      map((connections: Connections) => {
        return this.currentFlowService.filterConnectionsByPosition(
          connections,
          this.position
        );
      })
    );
  }

  gotoCreateConnection() {
    this.router.navigate(['/connections/create']);
  }

  onSelected(connection: Connection) {
    if (connection === undefined) {
      return this.gotoCreateConnection();
    }

    log.debugc(() => 'Selected connection: ' + connection.name, category);

    this.currentFlowService.events.emit({
      kind: 'integration-set-connection',
      position: this.position,
      connection: connection,
      onSave: () => {
        this.router.navigate(['action-select', this.position], {
          relativeTo: this.route.parent
        });
      }
    });
  }

  goBack() {
    const step = this.currentFlowService.getStep(this.position);
    step.connection = undefined;
    this.flowPageService.goBack(['save-or-add-step'], this.route);
  }

  loadConnections() {
    if (!this.currentFlowService.loaded) {
      return;
    }
    const step = this.currentFlowService.getStep(this.position);
    if (!step || step.stepKind !== 'endpoint') {
      /* Safety net */
      this.router.navigate(['save-or-add-step'], {
        relativeTo: this.route.parent
      });
      return;
    }
    if (step.connection) {
      this.router.navigate(['action-select', this.position], {
        relativeTo: this.route.parent
      });
      return;
    }
    this.store.loadAll();
    this.currentFlowService.events.emit({
      kind: 'integration-connection-select',
      position: this.position
    });
  }

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case 'integration-updated':
        this.loadConnections();
        break;
      default:
        break;
    }
  }

  ngOnInit() {
    this.routeSubscription = this.route.paramMap
      .pipe(first(params => params.has('position')))
      .subscribe(params => {
        const position = params.get('position');
        this.position = +position;
        this.loadConnections();
        this.positionText = this.getPositionText(this.position);
      });
  }

  ngOnDestroy() {
    this.routeSubscription.unsubscribe();
    if (this.flowSubscription) {
      this.flowSubscription.unsubscribe();
    }
  }

  private getPositionText(position) {
    if (position === 0) {
      return 'start';
    }
    if (position === this.currentFlowService.getLastPosition()) {
      return 'finish';
    }
    return '';
  }
}
