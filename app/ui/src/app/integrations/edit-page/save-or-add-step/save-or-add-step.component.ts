import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';

import { IntegrationStore } from '@syndesis/ui/store';
import { CurrentFlow, FlowEvent, FlowPage } from '@syndesis/ui/integrations';
import { Integration, Step, TypeFactory } from '@syndesis/ui/model';
import { log, getCategory } from '@syndesis/ui/logging';

const category = getCategory('IntegrationsCreatePage');
import { TourService } from 'ngx-tour-ngx-bootstrap';
import { UserService } from '@syndesis/ui/common';

@Component({
  selector: 'syndesis-integrations-save-or-add-step',
  templateUrl: 'save-or-add-step.component.html',
  styleUrls: ['./save-or-add-step.component.scss']
})
export class IntegrationsSaveOrAddStepComponent extends FlowPage implements OnInit, OnDestroy {
  integration: Integration;
  errorMessage: any = undefined;

  constructor(
    public currentFlow: CurrentFlow,
    public store: IntegrationStore,
    public route: ActivatedRoute,
    public router: Router,
    public tourService: TourService,
    private userService: UserService
  ) {
    super(currentFlow, route, router);
  }

  get currentStep() {
    return this.getCurrentStep();
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

  goBack() {
    /* this should be a no-op */
  }

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case 'integration-updated':
        this.validateFlow();
        break;
      default:
        break;
    }
  }

  addNew(type: string) {
    this.currentFlow.events.emit({
      kind: 'integration-add-step',
      type: type
    });
  }

  showPopouts(type: string) {
    this.currentFlow.events.emit({
      kind: 'integration-show-popouts',
      type: type
    });
  }

  insertStepAfter(position: number) {
    this.currentFlow.events.emit({
      kind: 'integration-insert-step',
      position: position,
      onSave: () => {
        this.router.navigate(['step-select', position + 1], {
          relativeTo: this.route
        });
      }
    });
  }

  insertConnectionAfter(position: number) {
    this.currentFlow.events.emit({
      kind: 'integration-insert-connection',
      position: position,
      onSave: () => {
        this.router.navigate(['connection-select', position + 1], {
          relativeTo: this.route
        });
      }
    });
  }

  startConnection() {
    return this.currentFlow.getStartStep();
  }

  endConnection() {
    return this.currentFlow.getEndStep();
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

  validateFlow() {
    if (!this.currentFlow.loaded) {
      return;
    }
    if (this.currentFlow.getStartConnection() === undefined) {
      this.router.navigate(
        ['connection-select', this.currentFlow.getFirstPosition()],
        { relativeTo: this.route.parent }
      );
      return;
    }
    if (this.currentFlow.getEndConnection() === undefined) {
      this.router.navigate(
        ['connection-select', this.currentFlow.getLastPosition()],
        { relativeTo: this.route.parent }
      );
      return;
    }
  }

  ngOnInit() {
    const validate = this.route.queryParams.map(
      params => params['validate'] || false
    );
    if (validate) {
      this.validateFlow();
    }

    /**
     * If guided tour state is set to be shown (i.e. true), then show it for this page, otherwise don't.
     */
    if (this.userService.getTourState() === true) {
      this.tourService.initialize([ {
        anchorId: 'integrations.step',
        title: 'Operate On Data',
        content: 'Clicking the plus sign lets you add an operation that the ' +
        'integration performs between the start and finish connections.',
        placement: 'right',
        } ],
      );
      setTimeout(() => this.tourService.start());
    }
  }
}
