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

  i: Integration = <Integration>{};
  flowSubscription: Subscription;
  childRouteSubscription: Subscription;
  urls: UrlSegment[];
  currentPosition: number;
  currentState: string;
  integrationName = '';

  constructor(
    private currentFlow: CurrentFlow,
    private route: ActivatedRoute,
    private router: Router,
    private detector: ChangeDetectorRef,
  ) {
    this.i.name = 'Integration Name';
  }

  getRowClass(position) {
    if (position === 0) {
      return 'start';
    }
    if (this.currentFlow.atEnd(position)) {
      return 'finish';
    }
    return '';
  }

  getIconClass(position) {
    const step = this.currentFlow.getStep(position);
    if (!step || !step['icon']) {
      return 'fa fa-plus';
    } else {
      return 'fa ' + step['icon'];
    }
  }

  getActiveClass(state, position) {
    if ((!state || state === this.currentState) && position === this.currentPosition) {
      return 'active';
    } else {
      return 'inactive';
    }
  }

  getTextClass(state, position) {
    if ((!state || state === this.currentState) && position === this.currentPosition) {
      return 'bold';
    } else {
      return '';
    }
  }

  getSteps() {
    const steps = (this.i || <Integration>{}).steps || [];
    // TODO hack, this is a new or partially defined integration
    if (!steps.length) {
      return [0, 0];
    }
    if (steps.length === 1) {
      return steps.concat(<Step>{});
    }
    return steps;
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
        this.detector.detectChanges();
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

  getConnectionText(position: number) {
    const step = this.currentFlow.getStep(position);
    if (step) {
      return step['name'];
    }
    // TODO this is wonky :-)
    if (position === 0) {
      return 'Start';
    }
    if (this.currentFlow.atEnd(position)) {
      return 'Finish';
    }
    return 'Set up this connection';
  }

  isCollapsed(position: number) {
    return this.currentFlow.getStep(position) !== undefined;
  }

  ngOnInit() {
    this.flowSubscription = this.currentFlow.events.subscribe((event: FlowEvent) => {
      this.handleFlowEvent(event);
    });
    log.debugc(() => 'Integration: ' + JSON.stringify(this.i));
  }

  ngOnDestroy() {
    this.flowSubscription.unsubscribe();
  }

}
