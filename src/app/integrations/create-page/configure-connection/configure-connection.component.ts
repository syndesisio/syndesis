import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';

import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { Connection } from '../../../store/connection/connection.model';

@Component({
  selector: 'ipaas-integrations-configure-connection',
  templateUrl: 'configure-connection.component.html',
})
export class IntegrationsConfigureConnectionComponent implements OnInit, OnDestroy {

  flowSubscription: Subscription;
  routeSubscription: Subscription;
  position: number;
  connection: Connection = <Connection>{ };

  constructor(
    private currentFlow: CurrentFlow,
    private route: ActivatedRoute,
    private router: Router,
  ) {

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
        this.connection = <Connection>this.currentFlow.integration.steps[this.position];
        this.currentFlow.events.emit({
          kind: 'integration-connection-configure',
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
