import { Component, Input, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { log, getCategory } from '../../../logging';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { Integration, Step } from '../../../model';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'ipaas-integrations-flow-view',
  templateUrl: './flow-view.component.html',
  styleUrls: ['./flow-view.component.scss'],
})
export class FlowViewComponent implements OnInit, OnDestroy {

  i: Integration;
  flowSubscription: Subscription;
  childRouteSubscription: Subscription;
  urls: UrlSegment[];
  @Input()
  currentPosition: number;
  @Input()
  currentState: string;
  integrationName = 'Integration Name';

  constructor(
    private currentFlow: CurrentFlow,
    private route: ActivatedRoute,
    private router: Router,
    private detector: ChangeDetectorRef,
  ) {
    // Hmmmmm, this needs to be set here to deal with new integrations
    this.flowSubscription = this.currentFlow.events.subscribe((event: FlowEvent) => {
      this.handleFlowEvent(event);
    });
  }

  loaded() {
    return this.i === undefined;
  }

  startConnection() {
    return this.currentFlow.getStep(this.firstPosition());
  }

  endConnection() {
    return this.currentFlow.getStep(this.lastPosition());
  }

  firstPosition() {
    return this.currentFlow.getFirstPosition();
  }

  lastPosition() {
    return this.currentFlow.getLastPosition();
  }

  getMiddleSteps() {
    return this.currentFlow.getMiddleSteps();
  }

  integrationNameChanged($event) {
    this.currentFlow.events.emit({
      kind: 'integration-set-name',
      name: $event,
    });
  }

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case 'integration-updated':
        this.i = event['integration'];
        this.integrationName = this.i.name;
        break;
      case 'integration-connection-select':
        break;
      case 'integration-connection-configure':
        break;
    }
    this.detector.detectChanges();
  }

  ngOnInit() {

  }

  ngOnDestroy() {
    this.flowSubscription.unsubscribe();
  }

}
