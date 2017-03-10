import { Component, Input } from '@angular/core';
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
export class FlowViewStepComponent {

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

  collapsed: boolean = undefined;

  constructor(
    private currentFlow: CurrentFlow,
    private route: ActivatedRoute,
    private router: Router,
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

  getStepKind(step) {
    if (step) {
      return step.stepKind;
    }
    return undefined;
  }

  getIconClass() {
    if (!this.step) {
      return 'fa fa-plus';
    }
    const step = this.step;
    switch ( step.stepKind ) {
      case 'endpoint':
        if (!this.step.connection) {
          return 'fa fa-plus';
        }
        return this.step.connection.icon ? 'fa ' + step.connection.icon : 'fa fa-airplane';
      case 'log':
        return 'fa fa-newspaper-o';
      default:
        return 'fa fa-plus';
    }
  }

  getPosition() {
    let position = this.position;
    if (this.position === -1) {
      position = this.currentFlow.getLastPosition();
    }
    return position;
  }

  getActiveClass(state) {
    if ((this.currentState === state || !state) && this.getPosition() === this.currentPosition) {
      if (this.collapsed) {
        this.collapsed = false;
      }
      return 'active';
    } else {
      if (!this.collapsed) {
        this.collapsed = true;
      }
      return 'inactive';
    }
  }

  getTextClass(state, addlClasses) {
    if ((this.currentState === state || !state) && this.getPosition() === this.currentPosition) {
      return 'current' + ' ' + addlClasses;
    } else {
      return addlClasses || '';
    }
  }

  goto(page: string) {
    this.router.navigate([page, this.getPosition()], { relativeTo: this.route });
  }

  getStepText() {
    if (!this.step) {
      return 'Set up this step';
    }
    switch (this.step.stepKind) {
      case 'endpoint':
        if (!this.step.connection) {
          return 'Set up this connection';
        }
        return this.step.connection.name;
      default:
        if (this.step.stepKind) {
          return this.step.stepKind;
        } else {
          return 'Set up this step';
        }
    }
  }

  toggleCollapsed() {
    if (this.getPosition() === this.currentPosition) {
      return;
    }
    this.collapsed = !this.collapsed;
  }

  isCollapsed() {
    if (this.collapsed === undefined) {
      this.collapsed = this.getPosition() !== this.currentPosition;
    }
    return this.collapsed;
  }
}
