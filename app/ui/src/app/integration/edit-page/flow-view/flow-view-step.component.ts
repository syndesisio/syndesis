import { Component, Input, ViewChild, OnChanges } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PopoverDirective } from 'ngx-bootstrap';

import { Action, Step, DataShape, DataShapeKinds } from '@syndesis/ui/platform';
import {
  CurrentFlowService,
  FlowPageService,
  INTEGRATION_INSERT_DATAMAPPER,
} from '@syndesis/ui/integration/edit-page';
import { INTEGRATION_DELETE_PROMPT } from '../edit-page.models';
import { SPLIT, ENDPOINT, LOG } from '@syndesis/ui/store';

@Component({
  selector: 'syndesis-integration-flow-view-step',
  templateUrl: './flow-view-step.component.html',
  styleUrls: ['./flow-view-step.component.scss'],
})
export class FlowViewStepComponent implements OnChanges {
  stepIndex: number;
  stepName: string;
  // the step object in the current flow
  @Input() step: Step;

  // the position in the integration flow
  @Input() position: number;

  // the current step in the flow the user is working with
  @Input() currentPosition: number;

  // the current state/page of the current step
  @Input() currentState: string;

  @Input() isApiProvider: boolean;
  @Input() isApiProviderOperationsPage: boolean;

  @ViewChild('datamapperInfoPop') datamapperInfoPop: PopoverDirective;

  inputDataShapeText: string;
  outputDataShapeText: string;
  previousStepShouldDefineDataShape = false;
  shouldAddDatamapper = false;
  isUnclosedSplit = false;

  constructor(
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router
  ) {}

  get currentStepKind() {
    return this.flowPageService.getCurrentStepKind(this.route);
  }

  toggleAddDatamapperInfo() {
    this.datamapperInfoPop.toggle();
  }

  hideAddDatamapperInfo() {
    this.datamapperInfoPop.hide();
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
      case ENDPOINT:
        if (!this.step.connection) {
          return 'fa fa-plus';
        }
        return this.step.connection.icon
          ? 'fa ' + step.connection.icon
          : 'fa fa-airplane';
      case LOG:
        return 'fa fa-newspaper-o';
      default:
        return '';
    }
  }

  showDelete() {
    if (this.currentState !== 'save-or-add-step') {
      return false;
    }
    if (
      this.currentFlowService.isApiProvider() &&
      (this.position === 0 || this.currentFlowService.atEnd(this.position))
    ) {
      return false;
    }
    return true;
  }

  deletePrompt() {
    this.currentFlowService.events.emit({
      kind: INTEGRATION_DELETE_PROMPT,
      position: this.getPosition(),
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
      case ENDPOINT:
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
      case ENDPOINT:
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

  gotoPageFor(step) {
    if (this.isApiProvider) {
      return;
    }
    switch (step.stepKind) {
      case ENDPOINT:
        if (!step.connection) {
          this.goto('step-select');
          return;
        }
        if (!step.action) {
          this.goto('action-select');
          return;
        }
        this.goto('action-configure', 0);
        return;
      default:
        if (!step.stepKind) {
          this.goto('step-select');
          return;
        }
        this.goto('step-configure');
    }
  }

  goto(page: string, index?: number) {
    if (!page) {
      if (!this.isCollapsed()) {
        // this means we're actually in this step, so don't change the view
        return;
      }
      page = 'step-select';
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
      relativeTo: this.route,
    });
  }

  isCollapsed() {
    return this.getPosition() !== this.currentPosition;
  }

  visitPreviousStepDescribeData() {
    this.datamapperInfoPop.hide();
    const index = this.currentFlowService.getPreviousStepIndexWithDataShape(
      this.position
    );
    this.router.navigate(['describe-data', index, 'output'], {
      relativeTo: this.route,
    });
  }

  addDataMapper() {
    this.datamapperInfoPop.hide();
    const position = this.getPosition();
    this.currentFlowService.events.emit({
      kind: INTEGRATION_INSERT_DATAMAPPER,
      position: position,
      onSave: () => {
        setTimeout(() => {
          this.router.navigate(['step-configure', position], {
            relativeTo: this.route,
          });
        }, 10);
      },
    });
  }

  ngOnChanges() {
    this.stepIndex = this.getStepIndex();
    this.stepName = this.getStepName(this.step);
    this.previousStepShouldDefineDataShape = false;
    this.shouldAddDatamapper = false;

    this.isUnclosedSplit =
      this.step.stepKind === SPLIT &&
      this.currentFlowService.getNextAggregateStep(this.position) === undefined;

    if (!this.step || !this.step.action || !this.step.action.descriptor) {
      return;
    }

    if (
      this.step !== this.currentFlowService.getStartStep() &&
      this.step.action.descriptor.inputDataShape
    ) {
      const inDataShape = this.step.action.descriptor.inputDataShape;
      this.inputDataShapeText = this.getDataShapeText(inDataShape);
      if (
        [DataShapeKinds.ANY, DataShapeKinds.NONE].indexOf(inDataShape.kind) ===
        -1
      ) {
        const prev = this.currentFlowService.getPreviousStepWithDataShape(
          this.position
        );
        if (prev && prev.action && prev.action.descriptor) {
          const prevOutDataShape = prev.action.descriptor.outputDataShape;
          if (DataShapeKinds.ANY === prevOutDataShape.kind) {
            this.previousStepShouldDefineDataShape = true;
          } else if (!this.isSameDataShape(inDataShape, prevOutDataShape)) {
            this.shouldAddDatamapper = true;
          }
        }
      }
    }

    if (
      this.step !== this.currentFlowService.getEndStep() &&
      this.step.action.descriptor.outputDataShape
    ) {
      this.outputDataShapeText = this.getDataShapeText(
        this.step.action.descriptor.outputDataShape
      );
    }
  }

  getFlowName() {
    return this.currentFlowService.getCurrentFlowName();
  }

  getFlowDescription() {
    return this.currentFlowService.getCurrentFlowDescription();
  }

  private getStepIndex() {
    return this.getPosition() + 1;
  }

  private getStepName(step: Step) {
    if (!step) {
      return 'Set up this step';
    }
    switch (step.stepKind) {
      case ENDPOINT:
        if (
          this.isApiProvider &&
          this.getPosition() === 0 &&
          !this.isApiProviderOperationsPage
        ) {
          return this.getFlowName();
        }
        if (step.action && step.action.name) {
          return step.action.name;
        }
        if (step.connection) {
          return step.connection.name;
        }
        if (this.getPosition() === 0) {
          return 'Start';
        }
        if (this.getPosition() === this.currentFlowService.getLastPosition()) {
          return 'Finish';
        }
        return 'Set up this connection';
      default:
        if (step.name) {
          return step.name;
        }
        return 'Set up this step';
    }
  }

  private getDataShapeText(dataShape: DataShape) {
    const isCollection =
      dataShape.metadata && dataShape.metadata.variant === 'collection';
    let answer = dataShape.name;
    if (dataShape.kind) {
      if (DataShapeKinds.ANY === dataShape.kind) {
        answer = 'ANY';
      } else if (DataShapeKinds.NONE === dataShape.kind) {
        answer = undefined;
      } else if (!dataShape.type) {
        answer = dataShape.kind;
      }
    }
    // TODO "split" currently appears to have variant set but maybe it shouldn't
    if (answer && isCollection && this.step.stepKind !== SPLIT) {
      answer = answer + ' (Collection)';
    }
    return answer;
  }

  private isSameDataShape(one: DataShape, other: DataShape): boolean {
    if (!one || !other) {
      return false;
    }
    return (
      one.kind === other.kind &&
      one.type === other.type &&
      one.specification === other.specification
    );
  }

  private thingIsEnabled(step: Step) {
    if (!step) {
      return false;
    }
    switch (step.stepKind) {
      case ENDPOINT:
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
