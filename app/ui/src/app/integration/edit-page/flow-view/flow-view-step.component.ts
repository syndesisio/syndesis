import { Component, Input, ViewChild, OnChanges } from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { PopoverDirective } from 'ngx-bootstrap/popover';

import { Action, Integration, Step, DataShape, DataShapeKinds } from '@syndesis/ui/platform';
import { ModalService } from '@syndesis/ui/common';
import { log, getCategory } from '@syndesis/ui/logging';
import { StepStore } from '@syndesis/ui/store';
import { CurrentFlowService, FlowEvent, FlowPageService } from '@syndesis/ui/integration/edit-page';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'syndesis-integration-flow-view-step',
  templateUrl: './flow-view-step.component.html',
  styleUrls: ['./flow-view-step.component.scss']
})
export class FlowViewStepComponent implements OnChanges {
  // the step object in the current flow
  @Input() step: Step;

  // the position in the integration flow
  @Input() position: number;

  // the current step in the flow the user is working with
  @Input() currentPosition: number;

  // the current state/page of the current step
  @Input() currentState: string;

  @ViewChild('pop') public pop: PopoverDirective;

  inputDataShapeText: string;
  outputDataShapeText: string;
  previousStepShouldDefineDataShape = false;
  shouldAddDatamapper = false;

  constructor(
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router,
    private stepStore: StepStore
  ) {

  }

  get currentStepKind() {
    return this.flowPageService.getCurrentStepKind(this.route);
  }

  showTooltip() {
    // TODO Apply UXD outcome - https://github.com/syndesisio/syndesis/issues/700
    // for now showing everything as a tooltip
    this.pop.show();
  }

  hideTooltip() {
    this.pop.hide();
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
    switch (step.stepKind) {
      case 'endpoint':
        if (!this.step.connection) {
          return 'fa fa-plus';
        }
        return this.step.connection.icon
          ? 'fa ' + step.connection.icon
          : 'fa fa-airplane';
      case 'log':
        return 'fa fa-newspaper-o';
      default:
        return '';
    }
  }

  showDelete() {
    if (this.currentState !== 'save-or-add-step') {
      return false;
    }
    return true;
  }

  deletePrompt() {
    this.currentFlowService.events.emit({
      kind: 'integration-delete-prompt',
      position: this.getPosition()
    });
  }

  getPosition() {
    let position = this.position;
    if (this.position === -1) {
      position = this.currentFlowService.getLastPosition();
    }
    return position;
  }

  getParentClass() {
    let clazz = '';
    if (this.getPosition() === this.currentPosition) {
      //clazz = 'current';
      clazz = 'active';
    }
    const step = this.currentFlowService.getStep(this.currentPosition);
    if (step && !this.stepIsComplete(step)) {
      clazz = clazz + ' disabled';
    }
    return 'parent-step ' + clazz;
  }

  getPropertyDefinitions(action: Action) {
    const descriptor: any = action.descriptor || {};
    const answer = descriptor.propertyDefinitionSteps || [];
    return answer;
  }

  getParentActiveClass() {
    let clazz = '';
    if (this.getPosition() === this.currentPosition) {
      clazz = 'active';
    } else {
      clazz = 'inactive';
    }
    return clazz;
  }

  getSubMenuActiveClass(state: string, page?: number) {
    if (!state) {
      if (this.thingIsEnabled(this.step)) {
        return 'active';
      } else {
        return 'inactive';
      }
    }
    let answer = 'inactive';
    if (
      (this.currentState === state || !state) &&
      this.getPosition() === this.currentPosition
    ) {
      answer = 'active';
    }
    if (this.getTextClass(state) !== 'active') {
      answer = answer + ' inactive';
    }
    const currentIndex = this.flowPageService.getCurrentStepIndex(this.route);
    if (page !== undefined && currentIndex >= 0) {
      if (page === currentIndex) {
        answer = 'active';
      } else {
        answer = 'inactive';
      }
    }
    return answer;
  }

  getConnectionClass() {
    if (this.step.stepKind === 'endpoint') {
      return '';
    }
    return 'not-connection';
  }

  getMenuCompleteClass(state: string) {
    switch (this.step.stepKind) {
      case 'endpoint':
        if (
          this.step.connection &&
          this.step.action &&
          this.step.configuredProperties
        ) {
          return 'complete';
        }
        break;
      default:
        if (this.step.stepKind && this.step.configuredProperties) {
          return 'complete';
        }
    }
    return 'incomplete';
  }

  getTextClass(state: string, page?: number) {
    switch (state) {
      case 'connection-select':
        if (this.step.connection) {
          return 'active';
        }
        break;
      case 'action-select':
        if (this.step.action) {
          return 'active';
        }
        break;
      case 'action-configure':
      case 'step-configure':
        if (this.step.configuredProperties) {
          return 'active';
        }
        break;
      case 'step-select':
        if (this.step.stepKind) {
          return 'active';
        }
        break;
      default:
        break;
    }
    if (
      (this.currentState === state || !state) &&
      this.getPosition() === this.currentPosition
    ) {
      return 'active';
    }
    return '';
  }

  stepIsComplete(step: Step) {
    switch (step.stepKind) {
      case 'endpoint':
        if (!step.connection || !step.action || !step.configuredProperties) {
          return false;
        }
        break;
      default:
        if (!step.stepKind || !step.configuredProperties) {
          return false;
        }
    }
    return true;
  }

  goto(page: string, index?: number) {
    if (!page) {
      if (!this.isCollapsed()) {
        // this means we're actually in this step, so don't change the view
        return;
      }
      // TODO wonder will there be more choices?
      switch (this.step.stepKind) {
        case 'endpoint':
          page = 'connection-select';
          break;
        default:
          page = 'step-select';
          break;
      }
    }
    // validate that the step is complete before we move
    const step = this.currentFlowService.getStep(this.currentPosition);
    // step can be null if we're on the save or add step page
    if (step && !this.stepIsComplete(step)) {
      return;
    }
    const route = [page, this.getPosition()];
    if (index !== undefined) {
      route.push(index);
    }
    this.router.navigate(route, {
      relativeTo: this.route
    });
  }

  getStepText() {
    if (!this.step) {
      return 'Set up this step';
    }
    const prefix = 'Step ' + (this.getPosition() + 1) + ' - ';
    switch (this.step.stepKind) {
      case 'endpoint':
        if (this.step.action && this.step.action.name) {
          return prefix + this.step.action.name;
        }
        if (this.step.connection) {
          return prefix + this.step.connection.name;
        }
        if (this.getPosition() === 0) {
          return prefix + 'Start';
        }
        if (this.getPosition() === this.currentFlowService.getLastPosition()) {
          return prefix + 'Finish';
        }
        return 'Set up this connection';
      default:
        if (this.step.name) {
          return prefix + this.step.name;
        }
        return 'Set up this step';
    }
  }

  isCollapsed() {
    return this.getPosition() !== this.currentPosition;
  }

  ngOnChanges() {
    this.previousStepShouldDefineDataShape = false;
    this.shouldAddDatamapper = false;
    if (!this.step || !this.step.action || !this.step.action.descriptor) {
      return;
    }

    if (this.step !== this.currentFlowService.getStartStep()
      && this.step.action.descriptor.inputDataShape) {
      const inDataShape = this.step.action.descriptor.inputDataShape;
      this.inputDataShapeText = this.getDataShapeText(inDataShape);
      if ([DataShapeKinds.ANY, DataShapeKinds.NONE].indexOf(inDataShape.kind) === -1) {
        const prev = this.currentFlowService.getPreviousStepWithDataShape(this.position);
        const prevOutDataShape = prev.action.descriptor.outputDataShape;
        if (DataShapeKinds.ANY === prevOutDataShape.kind) {
          this.previousStepShouldDefineDataShape = true;
        } else if (!this.isSameDataShape(inDataShape, prevOutDataShape)) {
          this.shouldAddDatamapper = true;
        }
      }
    }

    if (this.step !== this.currentFlowService.getEndStep()
      && this.step.action.descriptor.outputDataShape) {
      this.outputDataShapeText = this.getDataShapeText(this.step.action.descriptor.outputDataShape);
    }
  }

  private getDataShapeText(dataShape: DataShape) {
    if (dataShape.name) {
      return dataShape.name;
    }
    if (dataShape.kind) {
      if (DataShapeKinds.ANY === dataShape.kind) {
        return 'ANY';
      } else if (DataShapeKinds.NONE === dataShape.kind) {
        return undefined;
      }
    }
    return dataShape.type;
  }

  private isSameDataShape(one: DataShape, other: DataShape): boolean {
    if (!one || !other) {
      return false;
    }
    return one.kind === other.kind && one.type === other.type
      && one.specification === other.specification;
  }

  private thingIsEnabled(step: Step) {
    if (!step) {
      return false;
    }
    switch (step.stepKind) {
      case 'endpoint':
        if (step.connection && step.connection && step.configuredProperties) {
          return true;
        }
        break;
      default:
        if (step.configuredProperties) {
          return true;
        }
        break;
    }
    return false;
  }
}
