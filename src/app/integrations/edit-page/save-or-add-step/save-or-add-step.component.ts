import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { IntegrationStore } from '../../../store/integration/integration.store';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { FlowPage } from '../flow-page';
import { Integration, Step, TypeFactory } from '../../../model';
import { log, getCategory } from '../../../logging';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'ipaas-integrations-save-or-add-step',
  templateUrl: 'save-or-add-step.component.html',
  styleUrls: ['./save-or-add-step.component.scss'],
})
export class IntegrationsSaveOrAddStepComponent extends FlowPage implements OnInit, OnDestroy {

  integration: Integration;
  errorMessage: any = undefined;

  constructor(
    public currentFlow: CurrentFlow,
    public store: IntegrationStore,
    public route: ActivatedRoute,
    public router: Router,
    public detector: ChangeDetectorRef,
  ) {
    super(currentFlow, route, router);
  }
  get currentStep() {
    return this.getCurrentStep();
  }
  doSave() {
    if (!this.currentFlow.integration.name || this.currentFlow.integration.name === '') {
      this.router.navigate(['integration-basics'], { relativeTo: this.route.parent });
      return;
    }
    const router = this.router;
    this.currentFlow.events.emit({
      kind: 'integration-save',
      action: (i: Integration) => {
        router.navigate(['/integrations']);
      },
      error: (error) => {
        setTimeout(() => {
          this.errorMessage = error;
          this.detector.detectChanges();
        }, 10);
      },
    });
  }

  getCurrentPosition(route: ActivatedRoute = this.route): number {
    const child = route.firstChild;
    if (child && child.snapshot) {
      const path = child.snapshot.url;
      // log.debugc(() => 'path from root: ' + path, category);
      try {
        const position = path[1].path;
        return +position;
      } catch (error) {
        return -1;
      }
    } else {
      // log.debugc(() => 'no current child', category);
      return undefined;
    }
  }

  getCurrentStep(route: ActivatedRoute = this.route) {
    return this.currentFlow.getStep(this.getCurrentPosition(route));
  }

  goBack() { /* this should be a no-op */ }

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case 'integration-updated':
        this.validateFlow();
        break;
    }
  }

  addNew(type: string) {
    this.currentFlow.events.emit({
      kind: 'integration-add-step',
      type: type,
    });
  }

  showPopouts(type: string) {
    this.currentFlow.events.emit({
      kind: 'integration-show-popouts',
      type: type,
    });
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

  save() {
    this.currentFlow.integration.statusType = 'Deactivated';
    this.doSave();
  }

  saveAndPublish() {
    this.currentFlow.integration.statusType = 'Activated';
    this.doSave();
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
    //log.debugc(() => 'this.currentFlow.getMiddleSteps().length: ' + this.currentFlow.getMiddleSteps().length);
    return this.currentFlow.getMiddleSteps();
  }

  validateFlow() {
    if (this.currentFlow.getStartConnection() === undefined) {
      this.router.navigate(['connection-select', this.currentFlow.getFirstPosition()], { relativeTo: this.route.parent });
      return;
    }
    if (this.currentFlow.getEndConnection() === undefined) {
      this.router.navigate(['connection-select', this.currentFlow.getLastPosition()], { relativeTo: this.route.parent });
      return;
    }
  }

  ngOnInit() {
    const validate = this.route.queryParams.map(params => params['validate'] || false);
    if (validate) {
      this.validateFlow();
    }
  }

}
