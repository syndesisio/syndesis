import { Component, ElementRef, OnDestroy, ViewChild } from '@angular/core';
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';
import { Subscription } from 'rxjs';
import {
  CurrentFlowService,
  FlowEvent,
  FlowPageService
} from '@syndesis/ui/integration/edit-page';
import { ModalService } from '@syndesis/ui/common';
import { Integration } from '@syndesis/ui/platform';

@Component({
  selector: 'syndesis-integration-flow-view',
  templateUrl: './flow-view.component.html',
  styleUrls: ['./flow-view.component.scss']
})
export class FlowViewComponent implements OnDestroy {
  i: Integration;
  flowSubscription: Subscription;
  urls: UrlSegment[];
  selectedKind: string | boolean = false;
  editingName = false;
  isCollapsed = true;

  @ViewChild('nameInput') nameInput: ElementRef;

  constructor(
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router,
    private modalService: ModalService
  ) {
    this.flowSubscription = this.currentFlowService.events.subscribe(
      (event: FlowEvent) => {
        this.handleFlowEvent(event);
      }
    );
  }

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

  showAddStep() {
    return !this.selectedKind || this.selectedKind === 'step';
  }

  showAddConnection(selectedKind: string | boolean) {
    return !this.selectedKind || this.selectedKind === 'connection';
  }

  startEditingName() {
    this.editingName = true;
    this.nameInput.nativeElement.select();
  }

  stopEditingName() {
    this.editingName = false;
  }

  loaded() {
    return this.currentFlowService.loaded;
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

  isApiProvider() {
    try {
      return this.startConnection().connection.connectorId === 'api-provider';
    } catch (e) {
      // noop
    }
    return false;
  }

  isApiProviderOperationsPage() {
    return (
      this.router.url.startsWith('/integrations') &&
      this.router.url.endsWith('/operations')
    );
  }

  insertStepAfter(position: number) {
    this.selectedKind = undefined;
    this.currentFlowService.events.emit({
      kind: 'integration-insert-step',
      position: position,
      onSave: () => {
        setTimeout(() => {
          this.router.navigate(['step-select', position + 1], {
            relativeTo: this.route
          });
        }, 10);
      }
    });
  }

  get integrationName() {
    return (this.currentFlowService.integration || { name: '' }).name || '';
  }

  deletePrompt(position) {
    this.modalService.show('delete-step').then(modal => {
      if (modal.result) {
        const isFirst = position === this.currentFlowService.getFirstPosition();
        const isLast = position === this.currentFlowService.getLastPosition();

        this.currentFlowService.events.emit({
          kind: 'integration-remove-step',
          position: position,
          onSave: () => {
            setTimeout(() => {
              if (isFirst || isLast) {
                this.router.navigate(['step-select', position], {
                  relativeTo: this.route
                });
              } else {
                this.router.navigate(['save-or-add-step'], {
                  relativeTo: this.route
                });
              }
            }, 10);
          }
        });
      }
    });
  }

  set integrationName(name: string) {
    this.currentFlowService.events.emit({
      kind: 'integration-set-property',
      property: 'name',
      value: name
    });
  }

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case 'integration-updated':
        this.i = event['integration'];
        break;
      case 'integration-delete-prompt':
        this.deletePrompt(event['position']);
        break;
      default:
        break;
    }
  }

  ngOnDestroy() {
    if (this.flowSubscription) {
      this.flowSubscription.unsubscribe();
    }
  }
}
