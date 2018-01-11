import { Component, OnInit, OnDestroy } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { ActivatedRoute, Params, Router } from '@angular/router';

import { log, getCategory } from '@syndesis/ui/logging';
import { CurrentFlow, FlowEvent, FlowPage } from '@syndesis/ui/integrations';
import { ConnectionStore } from '@syndesis/ui/store';
import { Connections, Connection } from '@syndesis/ui/model';

const category = getCategory('Integrations');
import { TourService } from 'ngx-tour-ngx-bootstrap';
import { UserService } from '@syndesis/ui/common';

@Component({
  selector: 'syndesis-integrations-connection-select',
  templateUrl: 'connection-select.component.html',
  styleUrls: ['./connection-select.component.scss']
})
export class IntegrationsSelectConnectionComponent extends FlowPage implements OnInit, OnDestroy {
  loading: Observable<boolean>;
  connections: Observable<Connections>;
  filteredConnections = new BehaviorSubject(<Connections>{});
  position: number;

  constructor(
    public store: ConnectionStore,
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public tourService: TourService,
    private userService: UserService
  ) {
    super(currentFlow, route, router);
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

    this.currentFlow.events.emit({
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
    const step = this.currentFlow.getStep(this.position);
    step.connection = undefined;
    super.goBack(['save-or-add-step']);
  }

  positionText() {
    if (this.position === 0) {
      return 'start';
    }
    if (this.position === this.currentFlow.getLastPosition()) {
      return 'finish';
    }
    return '';
  }

  loadConnections() {
    if (!this.currentFlow.loaded) {
      return;
    }
    const step = this.currentFlow.getStep(this.position);
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
    this.currentFlow.events.emit({
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

    /**
     * If guided tour state is set to be shown (i.e. true), then show it for this page, otherwise don't.
     */
    if (this.userService.getTourState() === true) {
      this.tourService.initialize([{
        anchorId: 'integrations.panel',
        title: 'Integration Panel',
        content: 'As you create an integration, see its connections and steps ' +
          'in the order in which they occur when the integration is running.',
        placement: 'right',
      }, {
        anchorId: 'connections.available',
        title: 'Available Connections',
        content: 'After at least two connections are available, you can create ' +
          'an integration that uses the connections you choose.',
        placement: 'top',
      }],
      );
      setTimeout(() => this.tourService.start());
    }
  }

  ngOnDestroy() {
    super.ngOnDestroy();
  }
}
