import { Component, Input, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { log, getCategory } from '../../../logging';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { Integration, Step } from '../../../model';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'ipaas-integrations-flow-view-step',
  templateUrl: './flow-view-step.component.html',
  styleUrls: ['./flow-view-step.component.scss'],
})
export class FlowViewStepComponent implements OnInit, OnDestroy {

  // the step object in the current flow
  @Input()
  step: Step;

  // the position in the integration flow
  @Input()
  position: number;

  // the current step in the flow the user is working with
  @Input()
  currentPosition: number;

  // the current state/page of the current step
  @Input()
  currentState: string;

  constructor(
    private currentFlow: CurrentFlow,
    private detector: ChangeDetectorRef,
  ) {

  }

  getState() {
    return {
      step: this.step,
      position: this.position,
      currentPosition: this.currentPosition,
      currentState: this.currentState,
      firstPosition: this.currentFlow.getFirstPosition(),
      lastPosition: this.currentFlow.getLastPosition(),
    };
  }

  getIconClass(position) {
    if (!this.step || !this.step['icon']) {
      return 'fa fa-plus';
    } else {
      return 'fa ' + this.step['icon'];
    }
  }

  getActiveClass(state, position) {
    if (this.position === -1) {
      position = this.currentFlow.getLastPosition();
    }
    if ((!state || state === this.currentState) && position === this.currentPosition) {
      return 'active';
    } else {
      return 'inactive';
    }
  }

  getTextClass(state, position) {
    if (this.position === -1) {
      position = this.currentFlow.getLastPosition();
    }
    if ((!state || state === this.currentState) && position === this.currentPosition) {
      return 'bold';
    } else {
      return '';
    }
  }

  getConnectionText(position: number) {
    if (this.step) {
      return this.step['name'];
    }
    if (position === this.currentFlow.getFirstPosition()) {
      return 'Start';
    }
    if (position === this.currentFlow.getLastPosition()) {
      return 'Finish';
    }
    return 'Set up this connection';
  }

  isCollapsed(position: number) {
    return this.step !== undefined;
  }

  ngOnInit() {

  }

  ngOnDestroy() {

  }




}
