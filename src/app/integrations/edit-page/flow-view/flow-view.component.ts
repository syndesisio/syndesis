import { Component, Input, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { log, getCategory } from '../../../logging';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { Integration, Step, TypeFactory } from '../../../model';
import { ChildAwarePage } from '../child-aware-page';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'ipaas-integrations-flow-view',
  templateUrl: './flow-view.component.html',
  styleUrls: ['./flow-view.component.scss'],
})
export class FlowViewComponent extends ChildAwarePage implements OnInit, OnDestroy {

  i: Integration;
  flowSubscription: Subscription;
  childRouteSubscription: Subscription;
  urls: UrlSegment[];

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public detector: ChangeDetectorRef,
  ) {
    super(route, router);
    // Hmmmmm, this needs to be set here to deal with new integrations
    this.flowSubscription = this.currentFlow.events.subscribe((event: FlowEvent) => {
      this.handleFlowEvent(event);
    });
  }

  get currentPosition() {
    return this.getCurrentPosition();
  }

  get currentState() {
    return this.getCurrentChild();
  }

  editIntegrationBasics() {
    this.router.navigate(['integration-basics'], { relativeTo: this.route });
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

  insertStepAfter(position: number) {
    const target = position + 1;
    const step = TypeFactory.createStep();
    this.currentFlow.steps.splice(target, 0, step);
    this.router.navigate(['step-select', target], { relativeTo: this.route });
  }

  insertConnectionAfter(position: number) {
    const target = position + 1;
    const step = TypeFactory.createStep();
    step.stepKind = 'endpoint';
    this.currentFlow.steps.splice(target, 0, step);
    this.router.navigate(['connection-select', target], { relativeTo: this.route });
  }

  get integrationName() {
    return (this.currentFlow.integration || { name: '' }).name || '';
  }

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case 'integration-updated':
        this.i = event['integration'];
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
