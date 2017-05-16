import { Component, Input, OnInit, OnDestroy, ChangeDetectorRef, ViewChildren } from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { PopoverDirective } from 'ngx-bootstrap/popover';

import { log, getCategory } from '../../../logging';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { Integration, Step, TypeFactory } from '../../../model';
import { ChildAwarePage } from '../child-aware-page';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'syndesis-integrations-flow-view',
  templateUrl: './flow-view.component.html',
  styleUrls: ['./flow-view.component.scss'],
})
export class FlowViewComponent extends ChildAwarePage implements OnInit, OnDestroy {

  i: Integration;
  flowSubscription: Subscription;
  childRouteSubscription: Subscription;
  urls: UrlSegment[];
  selectedKind: string = undefined;
  @ViewChildren(PopoverDirective) popovers: PopoverDirective[];

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public detector: ChangeDetectorRef,
  ) {
    super(currentFlow, route, router);
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

  get containerClass() {
    switch (this.currentStepKind) {
      case 'mapper':
        return 'flow-view-container collapsed';
      default:
        return 'flow-view-container';
    }
  }

  editIntegrationBasics() {
    this.router.navigate(['integration-basics'], { relativeTo: this.route });
  }

  loaded() {
    return this.i === undefined;
  }

  get currentStep() {
    return this.getCurrentStep();
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
    const rc = this.currentFlow.getMiddleSteps();
    return rc;
  }

  insertStepAfter(position: number) {
    this.popovers.forEach((popover) => {
      popover.hide();
    });
    this.selectedKind = undefined;
    const target = position + 1;
    const step = TypeFactory.createStep();
    this.currentFlow.steps.splice(target, 0, step);
    this.router.navigate(['step-select', target], { relativeTo: this.route });
  }

  insertConnectionAfter(position: number) {
    this.popovers.forEach((popover) => {
      popover.hide();
    });
    this.selectedKind = undefined;
    const target = position + 1;
    const step = TypeFactory.createStep();
    step.stepKind = 'endpoint';
    this.currentFlow.steps.splice(target, 0, step);
    this.router.navigate(['connection-select', target], { relativeTo: this.route });
  }

  get integrationName() {
    return (this.currentFlow.integration || { name: '' }).name || '';
  }

  maybeShowPopover(popover: PopoverDirective) {
    if (this.getMiddleSteps() && !this.getMiddleSteps().length && popover && !popover.isOpen) {
      setTimeout( () => {
        popover.show();
      }, 10);
    }
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
      case 'integration-add-step':
          switch (event['type']) {
            case 'connection':
              this.insertConnectionAfter(0);
              return;
            case 'step':
              this.insertStepAfter(0);
              return;
          }
        break;
      case 'integration-show-popouts':
        this.selectedKind = event['type'];
        this.popovers.forEach((popover) => {
          popover.show();
        });
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
