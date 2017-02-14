import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { log, getCategory } from '../../../logging';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { Integration } from '../../../store/integration/integration.model';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'ipaas-integrations-flow-view',
  templateUrl: './flow-view.component.html',
  styleUrls: ['./flow-view.component.scss'],
})
export class FlowViewComponent implements OnInit, OnDestroy {

  i: Integration = <Integration>{};
  flowSubscription: Subscription;
  childRouteSubscription: Subscription;
  urls: UrlSegment[];
  currentPosition: number;
  currentState: string;

  constructor(
    private currentFlow: CurrentFlow,
    private route: ActivatedRoute,
    private router: Router,
  ) {

  }

  getClass(state, position) {
    if (state === this.currentState && position === this.currentPosition) {
      return 'bold';
    } else {
      return '';
    }
  }

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case 'integration-updated':
        this.i = event['integration'];
        break;
      case 'integration-connection-select':
        this.currentState = 'connection-select';
        this.currentPosition = event['position'];
        break;
      case 'integration-connection-configure':
        this.currentState = 'connection-configure';
        this.currentPosition = event['position'];
        break;
    }
  }

  ngOnInit() {
    this.flowSubscription = this.currentFlow.events.subscribe((event: FlowEvent) => {
      this.handleFlowEvent(event);
    });
  }

  ngOnDestroy() {
    this.flowSubscription.unsubscribe();
  }

}
