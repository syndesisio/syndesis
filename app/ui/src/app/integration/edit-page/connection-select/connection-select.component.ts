import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';

import { log, getCategory } from '@syndesis/ui/logging';
import { Connections, Connection, UserService } from '@syndesis/ui/platform';
import { CurrentFlowService, FlowEvent, FlowPageService } from '@syndesis/ui/integration/edit-page';
import { ConnectionStore } from '@syndesis/ui/store';

const category = getCategory('Integrations');

@Component({
  selector: 'syndesis-integration-connection-select',
  templateUrl: 'connection-select.component.html',
  styleUrls: ['./connection-select.component.scss']
})
export class IntegrationSelectConnectionComponent implements OnInit, OnDestroy {
  flowSubscription: Subscription;
  loading: Observable<boolean>;
  connections: Observable<Connections>;
  filteredConnections = new BehaviorSubject(<Connections>{});
  position: number;

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
    this.loading = store.loading;
    this.connections = store.list;
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

  positionText() {
    if (this.position === 0) {
      return 'start';
    }
    if (this.position === this.currentFlowService.getLastPosition()) {
      return 'finish';
    }
    return '';
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
    this.route.paramMap
      .first(params => params.has('position'))
      .subscribe(params => {
        this.position = +params.get('position');
        this.loadConnections();
      });
  }

  ngOnDestroy() {
    if (this.flowSubscription) {
      this.flowSubscription.unsubscribe();
    }
  }
}
