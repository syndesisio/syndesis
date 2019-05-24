import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';
import {
  CurrentFlowService,
  FlowPageService,
  INTEGRATION_INSERT_STEP,
  INTEGRATION_REMOVE_STEP,
} from '@syndesis/ui/integration/edit-page';
import { ModalService } from '@syndesis/ui/common';
import { Subscription } from 'rxjs';
import {
  INTEGRATION_DELETE_PROMPT,
  INTEGRATION_SIDEBAR_COLLAPSE,
  INTEGRATION_SIDEBAR_EXPAND,
  FlowEvent,
} from '../edit-page.models';

@Component({
  selector: 'syndesis-integration-flow-view',
  templateUrl: './flow-view.component.html',
  styleUrls: ['./flow-view.component.scss'],
})
export class FlowViewComponent implements OnInit, OnDestroy {
  urls: UrlSegment[];
  isCollapsed = false;
  flowSubscription: Subscription;

  constructor(
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router,
    private modalService: ModalService
  ) {}

  get currentPosition() {
    return this.flowPageService.getCurrentPosition(this.route);
  }

  get currentState() {
    return this.flowPageService.getCurrentChild(this.route);
  }

  get currentStepKind() {
    return this.flowPageService.getCurrentStepKind(this.route);
  }

  toggleCollapsed() {
    this.isCollapsed = !this.isCollapsed;
  }

  get currentStep() {
    return this.flowPageService.getCurrentStep(this.route);
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

  isAlternateFlow() {
    return this.currentFlowService.isAlternateFlow();
  }

  isApiProvider() {
    return this.currentFlowService.isApiProvider();
  }

  isApiProviderOperationsPage() {
    return (
      this.router.url.startsWith('/integrations') &&
      this.router.url.endsWith('/operations')
    );
  }

  insertStepAfter(position: number) {
    this.currentFlowService.events.emit({
      kind: INTEGRATION_INSERT_STEP,
      position: position,
      onSave: () => {
        setTimeout(() => {
          this.router.navigate(['step-select', position + 1], {
            relativeTo: this.route,
          });
        }, 10);
      },
    });
  }

  deletePrompt(position) {
    this.modalService.show('delete-step').then(modal => {
      if (modal.result) {
        const isFirst = position === this.currentFlowService.getFirstPosition();
        const isLast = position === this.currentFlowService.getLastPosition();
        this.currentFlowService.events.emit({
          kind: INTEGRATION_REMOVE_STEP,
          position: position,
          onSave: () => {
            setTimeout(() => {
              if (isFirst || isLast) {
                this.router.navigate(['step-select', position], {
                  relativeTo: this.route,
                });
              } else {
                this.router.navigate(['save-or-add-step'], {
                  relativeTo: this.route,
                });
              }
            }, 10);
          },
        });
      }
    });
  }

  ngOnInit() {
    this.flowSubscription = this.currentFlowService.events.subscribe(
      (event: FlowEvent) => {
        switch (event.kind) {
          case INTEGRATION_DELETE_PROMPT:
            this.deletePrompt(event.position);
            break;

          case INTEGRATION_SIDEBAR_COLLAPSE:
            setTimeout(() => (this.isCollapsed = true), 1);
            break;

          case INTEGRATION_SIDEBAR_EXPAND:
            setTimeout(() => (this.isCollapsed = false), 1);
            break;
          default:
        }
      }
    );
  }

  ngOnDestroy() {
    if (this.flowSubscription) {
      this.flowSubscription.unsubscribe();
    }
  }
}
