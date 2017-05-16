import { Component, Input, ViewChild } from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { ModalDirective } from 'ngx-bootstrap/modal';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { PopoverDirective } from 'ngx-bootstrap/popover';

import { ChildAwarePage } from '../child-aware-page';
import { log, getCategory } from '../../../logging';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { Integration, Step } from '../../../model';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'syndesis-integrations-flow-view-step',
  templateUrl: './flow-view-step.component.html',
  styleUrls: ['./flow-view-step.component.scss'],
})
export class FlowViewStepComponent extends ChildAwarePage {

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

  @ViewChild('childModal') public deleteModal: ModalDirective;
  @ViewChild('pop') public pop: PopoverDirective;

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
  ) {
    super(currentFlow, route, router);
  }

  showTooltip() {
    if (this.step.stepKind === 'endpoint') {
      this.pop.show();
    }
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
    switch ( step.stepKind ) {
      case 'endpoint':
        if (!this.step.connection) {
          return 'fa fa-plus';
        }
        return this.step.connection.icon ? 'fa ' + step.connection.icon : 'fa fa-airplane';
      case 'log':
        return 'fa fa-newspaper-o';
      default:
        return '';
    }
  }

  showDelete() {
    switch (this.step.stepKind) {
      case 'endpoint':
        return this.step.connection && this.step.action && this.step.configuredProperties;
      default:
        return this.step.stepKind && this.step.configuredProperties;
    }
  }

  deletePrompt() {
    this.deleteModal.show();
  }

  cancelDeletePrompt() {
    this.deleteModal.hide();
  }

  deleteStep() {
    this.deleteModal.hide();
    const position = this.getPosition();
    const isFirst = position === this.currentFlow.getFirstPosition();
    const isLast = position === this.currentFlow.getLastPosition();

    this.currentFlow.events.emit({
      kind: 'integration-remove-step',
      position: position,
      onSave: () => {
        if (isFirst) {
          this.router.navigate(['connection-select', position], { relativeTo: this.route });
        } else if (isLast) {
          this.router.navigate(['connection-select', position], { relativeTo: this.route });
        } else {
          this.router.navigate(['save-or-add-step'], { relativeTo: this.route });
        }
      },
    });
  }

  getPosition() {
    let position = this.position;
    if (this.position === -1) {
      position = this.currentFlow.getLastPosition();
    }
    return position;
  }

  getParentClass() {
    let clazz = '';
    if (this.getPosition() === this.currentPosition) {
      //clazz = 'current';
      clazz = 'active';
    }
    return 'parent-step ' + clazz;
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

  getParentActiveClass() {
    let clazz = '';
    if (this.getPosition() === this.currentPosition) {
      clazz = 'active';
    } else {
      clazz = 'inactive';
    }
    return clazz;
  }

  getSubMenuActiveClass(state: string) {
    if (!state) {
      if (this.thingIsEnabled(this.step)) {
        return 'active';
      } else {
        return 'inactive';
      }
    }
    let answer = 'inactive';
    if ((this.currentState === state || !state) && this.getPosition() === this.currentPosition) {
      answer = 'active';
    }
    if (this.getTextClass(state) !== 'active') {
      answer = answer + ' inactive';
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
        if (this.step.connection && this.step.action && this.step.configuredProperties) {
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

  getTextClass(state: string) {
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
    }
    if ((this.currentState === state || !state) && this.getPosition() === this.currentPosition) {
      return 'active';
    }
    return '';
  }

  goto(page: string) {
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
    this.router.navigate([page, this.getPosition()], { relativeTo: this.route });
  }

  getStepText() {
    if (!this.step) {
      return 'Set up this step';
    }
    switch (this.step.stepKind) {
      case 'endpoint':
        if (this.step.action && this.step.action.name) {
          return this.step.action.name;
        }
        if (this.step.connection) {
          return this.step.connection.name;
        }
        if (this.getPosition() === 0) {
          return 'Start';
        }
        if (this.getPosition() === this.currentFlow.getLastPosition()) {
          return 'Finish';
        }
        return 'Set up this connection';
      default:
        if (this.step.stepKind) {
          return this.step.stepKind;
        } else {
          return 'Set up this step';
        }
    }
  }

  isCollapsed() {
    return this.getPosition() !== this.currentPosition;
  }
}
