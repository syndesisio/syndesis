import { map } from 'rxjs/operators';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import {
  FlowEvent,
  CurrentFlowService,
  FlowPageService
} from '@syndesis/ui/integration/edit-page';
import { IntegrationStore } from '@syndesis/ui/store';
import { log, getCategory } from '@syndesis/ui/logging';
import { Integration } from '@syndesis/ui/platform';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'syndesis-integration-save-or-add-step',
  templateUrl: 'save-or-add-step.component.html',
  styleUrls: [
    '../../integration-common.scss',
    './save-or-add-step.component.scss'
  ]
})
export class IntegrationSaveOrAddStepComponent implements OnInit {
  integration: Integration;

  constructor(
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router
  ) {}

  get errorMessage() {
    return this.flowPageService.errorMessage;
  }

  get saveInProgress() {
    return this.flowPageService.saveInProgress;
  }

  get publishInProgress() {
    return this.flowPageService.publishInProgress;
  }

  cancel() {
    this.flowPageService.cancel();
  }

  save() {
    this.flowPageService.save(this.route);
  }

  publish() {
    this.flowPageService.publish(this.route);
  }

  get currentStep() {
    return this.flowPageService.getCurrentStep(this.route);
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
    this.currentFlowService.events.emit({
      kind: 'integration-add-step',
      type: type
    });
  }

  showPopouts(type: string) {
    this.currentFlowService.events.emit({
      kind: 'integration-show-popouts',
      type: type
    });
  }

  insertStepAfter(position: number) {
    this.currentFlowService.events.emit({
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
    this.currentFlowService.events.emit({
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
    return this.currentFlowService.getStartStep();
  }

  endConnection() {
    return this.currentFlowService.getEndStep();
  }

  firstPosition() {
    return this.currentFlowService.getFirstPosition();
  }

  lastPosition() {
    return this.currentFlowService.getLastPosition();
  }

  getMiddleSteps() {
    return this.currentFlowService.getMiddleSteps();
  }

  validateFlow() {
    if (!this.currentFlowService.loaded) {
      return;
    }
    if (this.currentFlowService.getStartConnection() === undefined) {
      this.router.navigate(
        ['connection-select', this.currentFlowService.getFirstPosition()],
        { relativeTo: this.route.parent }
      );
      return;
    }
    if (this.currentFlowService.getEndConnection() === undefined) {
      this.router.navigate(
        ['connection-select', this.currentFlowService.getLastPosition()],
        { relativeTo: this.route.parent }
      );
      return;
    }
  }

  ngOnInit() {
    this.flowPageService.initialize();
    const validate = this.route.queryParams.pipe(
      map(params => params['validate'] || false)
    );
    if (validate) {
      this.validateFlow();
    }
  }
}
