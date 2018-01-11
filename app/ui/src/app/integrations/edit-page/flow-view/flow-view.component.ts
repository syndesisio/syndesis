import {
  Component,
  ElementRef,
  Input,
  OnInit,
  OnDestroy,
  ViewChild,
  ViewChildren
} from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { PopoverDirective } from 'ngx-bootstrap/popover';
import { log, getCategory } from '@syndesis/ui/logging';
import { ChildAwarePage, CurrentFlow, FlowEvent } from '@syndesis/ui/integrations/edit-page';
import { Integration, Step, TypeFactory } from '@syndesis/ui/model';
import { TourService } from 'ngx-tour-ngx-bootstrap';
import { UserService, ModalService } from '@syndesis/ui/common';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'syndesis-integrations-flow-view',
  templateUrl: './flow-view.component.html',
  styleUrls: ['./flow-view.component.scss']
})
export class FlowViewComponent extends ChildAwarePage
  implements OnInit, OnDestroy {
  i: Integration;
  flowSubscription: Subscription;
  urls: UrlSegment[];
  selectedKind: string | boolean = false;
  editingName = false;

  @ViewChildren(PopoverDirective) popovers: PopoverDirective[];
  @ViewChild('nameInput') nameInput: ElementRef;

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public tourService: TourService,
    private userService: UserService,
    private modalService: ModalService
  ) {
    super(currentFlow, route, router);
    this.flowSubscription = this.currentFlow.events.subscribe(
      (event: FlowEvent) => {
        this.handleFlowEvent(event);
      }
    );
  }

  get currentPosition() {
    return this.getCurrentPosition();
  }

  get currentState() {
    return this.getCurrentChild();
  }

  get containerClass() {
    switch (this.currentStepKind) {
      case 'mapper':
        return 'flow-view-container collapsed';
      default:
        return 'flow-view-container';
    }
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
    return this.currentFlow.loaded;
  }

  get currentStep() {
    return this.getCurrentStep();
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

  insertStepAfter(position: number) {
    this.popovers.forEach(popover => popover.hide());

    this.selectedKind = undefined;

    this.currentFlow.events.emit({
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

    this.currentFlow.events.emit({
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
    return (this.currentFlow.integration || { name: '' }).name || '';
  }

  deletePrompt(position) {
    this.modalService
      .show('delete-step')
      .then(modal => {
        if (modal.result) {
          const isFirst = position === this.currentFlow.getFirstPosition();
          const isLast = position === this.currentFlow.getLastPosition();

          this.currentFlow.events.emit({
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
    this.currentFlow.events.emit({
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

  ngOnInit() {
    /**
     * If guided tour state is set to be shown (i.e. true), then show it for this page, otherwise don't.
     */
    if (this.userService.getTourState() === true) {
      this.tourService.initialize([{
        anchorId: 'integrations.step',
        title: 'Operate On Data',
        content: 'Clicking the plus sign lets you add an operation that the integration performs between the start and finish connections.',
        placement: 'right',
      }],
      );
      setTimeout(() => this.tourService.start());
    }
  }

  ngOnDestroy() {
    if (this.flowSubscription) {
      this.flowSubscription.unsubscribe();
    }
  }
}
