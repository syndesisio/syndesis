import { map } from 'rxjs/operators';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import {
  FlowEvent,
  CurrentFlowService,
  FlowPageService,
  INTEGRATION_SET_PROPERTIES,
  INTEGRATION_CANCEL_CLICKED,
} from '@syndesis/ui/integration/edit-page';
import { Integration, IntegrationType } from '@syndesis/ui/platform';
import { INTEGRATION_SIDEBAR_EXPAND } from '../edit-page.models';

@Component({
  selector: 'syndesis-integration-save-or-add-step',
  templateUrl: 'save-or-add-step.component.html',
  styleUrls: [
    '../../integration-common.scss',
    './save-or-add-step.component.scss',
  ],
})
export class IntegrationSaveOrAddStepComponent implements OnInit, OnDestroy {
  integration: Integration;
  IntegrationType = IntegrationType;
  flowSubscription: any;

  constructor(
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router
  ) {}

  get errorMessage() {
    return this.flowPageService.errorMessage;
  }

  get currentStep() {
    return this.flowPageService.getCurrentStep(this.route);
  }

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case INTEGRATION_CANCEL_CLICKED:
        if (this.currentFlowService.integration.id) {
          this.router.navigate([
            '/integrations',
            this.currentFlowService.integration.id,
          ]);
        } else {
          this.router.navigate(['/integrations']);
        }
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

  ngOnInit() {
    this.flowSubscription = this.currentFlowService.events.subscribe(event => {
      this.handleFlowEvent(event);
    });
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
            kind: INTEGRATION_SET_PROPERTIES,
            position: this.currentFlowService.getLastPosition(),
            properties: lastStep.configuredProperties,
          });
        }
      }
    }
    this.flowPageService.initialize();
    const validate = this.route.queryParams.pipe(
      map(params => params['validate'] || false)
    );
    if (validate) {
      this.currentFlowService.validateFlowAndMaybeRedirect(
        this.route,
        this.router
      );
    }
    // Show the full sidebar 'cause this page doesn't have a lot
    this.currentFlowService.events.emit({
      kind: INTEGRATION_SIDEBAR_EXPAND,
    });
  }

  ngOnDestroy() {
    if (this.flowSubscription) {
      this.flowSubscription.unsubscribe();
    }
  }
}
