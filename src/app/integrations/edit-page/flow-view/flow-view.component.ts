import {
  Component,
  ElementRef,
  Input,
  OnInit,
  OnDestroy,
  ChangeDetectorRef,
  ViewChild,
  ViewChildren,
} from '@angular/core';
import { ActivatedRoute, Params, Router, UrlSegment } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { PopoverDirective } from 'ngx-bootstrap/popover';

import { log, getCategory } from '../../../logging';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { Integration, Step, TypeFactory } from '../../../model';
import { ChildAwarePage } from '../child-aware-page';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'syndesis-integrations-flow-view',
  templateUrl: './flow-view.component.html',
  styleUrls: ['./flow-view.component.scss'],
})
export class FlowViewComponent extends ChildAwarePage
  implements OnInit, OnDestroy {
  i: Integration;
  flowSubscription: Subscription;
  routeSubscription: Subscription;
  urls: UrlSegment[];
  selectedKind: string | boolean = false;
  editingName = false;

  @ViewChildren(PopoverDirective) popovers: PopoverDirective[];
  @ViewChild('nameInput') nameInput: ElementRef;

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public detector: ChangeDetectorRef,
  ) {
    super(currentFlow, route, router);
    this.flowSubscription = this.currentFlow.events.subscribe(
      (event: FlowEvent) => {
        this.handleFlowEvent(event);
      },
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
    return this.currentFlow._loaded;
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
    this.popovers.forEach(popover => {
      popover.hide();
    });
    this.selectedKind = undefined;
    this.currentFlow.events.emit({
      kind: 'integration-insert-step',
      position: position,
      onSave: () => {
        setTimeout(() => {
          try {
            this.detector.detectChanges();
          } catch (err) {}
          this.router.navigate(['step-select', position + 1], {
            relativeTo: this.route,
          });
        }, 10);
      },
    });
  }

  insertConnectionAfter(position: number) {
    this.popovers.forEach(popover => {
      popover.hide();
    });
    this.currentFlow.events.emit({
      kind: 'integration-insert-connection',
      position: position,
      onSave: () => {
        setTimeout(() => {
          try {
            this.detector.detectChanges();
          } catch (err) {}
          this.router.navigate(['connection-select', position + 1], {
            relativeTo: this.route,
          });
        }, 10);
      },
    });
  }

  get integrationName() {
    return (this.currentFlow.integration || { name: '' }).name || '';
  }

  set integrationName(name: string) {
    this.currentFlow.events.emit({
      kind: 'integration-set-property',
      property: 'name',
      value: name,
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
        this.detector.detectChanges();
      }, 10);
    }
  }

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case 'integration-updated':
        this.i = event['integration'];
        setTimeout(() => this.maybeShowPopover(this.popovers[0]), 50);
        break;
      case 'integration-connection-select':
        break;
      case 'integration-connection-configure':
        break;
      case 'integration-add-step':
        switch (event['type']) {
          case 'connection':
            this.insertConnectionAfter(0);
            return;
          case 'step':
            this.insertStepAfter(0);
            return;
        }
        break;
      case 'integration-show-popouts':
        this.selectedKind = event['type'] || false;
        this.popovers.forEach(popover => popover.show());
        break;
    }
    try {
      this.detector.detectChanges();
    } catch (err) {}
  }

  ngOnInit() {
    this.routeSubscription = this.router.events.subscribe(event => {
      try {
        this.detector.detectChanges();
      } catch (err) {}
    });
  }

  ngOnDestroy() {
    this.routeSubscription.unsubscribe();
    this.flowSubscription.unsubscribe();
  }
}
