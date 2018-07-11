import {
  Component,
  ElementRef,
  OnDestroy,
  ViewChild,
  ViewChildren
} from '@angular/core';
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';
import { Subscription } from 'rxjs';
import { PopoverDirective } from 'ngx-bootstrap/popover';
import { log, getCategory } from '@syndesis/ui/logging';
import {
  CurrentFlowService,
  FlowEvent,
  FlowPageService
} from '@syndesis/ui/integration/edit-page';
import { ModalService } from '@syndesis/ui/common';
import { Integration, UserService } from '@syndesis/ui/platform';

const category = getCategory('IntegrationsCreatePage');

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

  @ViewChildren(PopoverDirective) popovers: PopoverDirective[];
  @ViewChild('nameInput') nameInput: ElementRef;

  constructor(
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router,
    private userService: UserService,
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

  /*
  get containerClass() {
    switch (this.flowPageService.getCurrentStepKind(this.route)) {
      case 'mapper':
        return true;
      default:
        return false;
    }
  }
  */

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

  insertStepAfter(position: number) {
    this.popovers.forEach(popover => popover.hide());

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

  insertConnectionAfter(position: number) {
    this.popovers.forEach(popover => popover.hide());

    this.currentFlowService.events.emit({
      kind: 'integration-insert-connection',
      position: position,
      onSave: () => {
        setTimeout(() => {
          this.router.navigate(['connection-select', position + 1], {
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
                this.router.navigate(['connection-select', position], {
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

  maybeShowPopover(popover: PopoverDirective) {
    if (
      this.getMiddleSteps() &&
      !this.getMiddleSteps().length &&
      popover &&
      !popover.isOpen
    ) {
      setTimeout(() => {
        popover.show();
      }, 10);
    }
  }

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case 'integration-updated':
        this.i = event['integration'];
        setTimeout(() => this.maybeShowPopover(this.popovers[0]), 50);
        break;
      case 'integration-add-step':
        switch (event['type']) {
          case 'connection':
            this.insertConnectionAfter(0);
            break;
          case 'step':
            this.insertStepAfter(0);
            break;
          default:
            break;
        }
        break;
      case 'integration-show-popouts':
        this.selectedKind = event['type'] || false;
        this.popovers.forEach(popover => popover.show());
        break;
      case 'integration-delete-prompt':
        this.deletePrompt(event['position']);
        break;
      case 'integration-connection-select':
      case 'integration-connection-configure':
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
