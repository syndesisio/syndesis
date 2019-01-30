import { map } from 'rxjs/operators';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import {
  FlowEvent,
  CurrentFlowService,
  FlowPageService
} from '@syndesis/ui/integration/edit-page';
import { Integration, IntegrationType } from '@syndesis/ui/platform';

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
  IntegrationType = IntegrationType;

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

  save(targetRoute) {
    this.flowPageService.save(this.route, targetRoute);
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
    if (
      !this.currentFlowService.getStartStep() ||
      typeof this.currentFlowService.getStartStep().connection === 'undefined'
    ) {
      this.router.navigate(
        ['step-select', this.currentFlowService.getFirstPosition()],
        {
          relativeTo: this.route.parent
        }
      );
      return;
    }
    if (
      !this.currentFlowService.getEndStep() ||
      typeof this.currentFlowService.getEndStep().connection === 'undefined'
    ) {
      this.router.navigate(
        ['step-select', this.currentFlowService.getLastPosition()],
        {
          relativeTo: this.route.parent
        }
      );
      return;
    }
  }

  ngOnInit() {
    const lastStep = this.currentFlowService.getEndStep();
    if (
      lastStep &&
      lastStep.action &&
      lastStep.action.id == 'io.syndesis:api-provider-end'
    ) {
      if (lastStep.configuredProperties['httpResponseCode'] == '501') {
        const returnCode = this.currentFlowService.currentFlow.metadata[
          'default-return-code'
        ];
        const returnCodeEdited = this.currentFlowService.currentFlow.metadata[
          'return-code-edited'
        ];
        if (returnCode && !returnCodeEdited) {
          this.currentFlowService.currentFlow.metadata['return-code-edited'] =
            'true';
          lastStep.configuredProperties['httpResponseCode'] = returnCode;
          // current flow service returns copies of steps nowadays
          this.currentFlowService.events.emit({
            kind: 'integration-set-properties',
            position: this.currentFlowService.getLastPosition(),
            properties: lastStep.configuredProperties
          });
        }
      }
    }
    this.flowPageService.initialize();
    const validate = this.route.queryParams.pipe(
      map(params => params['validate'] || false)
    );
    if (validate) {
      this.validateFlow();
    }
  }
}
