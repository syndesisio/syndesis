import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';

import { StepStore, StepKind, StepKinds } from '../../../store/step/step.store';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { FlowPage } from '../flow-page';
import { Step, Steps, TypeFactory } from '../../../model';
import { log, getCategory } from '../../../logging';

@Component({
  selector: 'syndesis-integrations-step-select',
  templateUrl: './step-select.component.html',
  styleUrls: ['./step-select.component.scss']
})
export class IntegrationsStepSelectComponent extends FlowPage
  implements OnInit, OnDestroy {
  steps: Steps;
  position: number;

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public stepStore: StepStore,
  ) {
    super(currentFlow, route, router);
  }

  goBack() {
    super.goBack(['save-or-add-step']);
  }

  getName(step: StepKind) {
    return this.stepStore.getStepName(step.stepKind);
  }

  getDescription(step: StepKind) {
    return this.stepStore.getStepDescription(step.stepKind);
  }

  isSelected(step: Step) {
    log.debugc(() => 'Step: ' + step);
    const _step = this.currentFlow.getStep(this.position);
    return _step && step.stepKind === _step.stepKind;
  }

  handleFlowEvent(event: FlowEvent) {
    const step = this.currentFlow.getStep(this.position);
    switch (event.kind) {
      case 'integration-step-select':
        step.stepKind = undefined;
        break;
      case 'integration-updated':
        if (!step) {
          // safety net
          this.router.navigate(['save-or-add-step'], {
            relativeTo: this.route.parent
          });
          return;
        }
        if (step.configuredProperties) {
          this.router.navigate(['step-configure', this.position], {
            relativeTo: this.route.parent
          });
          return;
        }
        break;
      default:
        break;
    }
  }

  onSelect(step: Step) {
    // TODO hack to use the existing step in the flow if it matches
    const _step = this.currentFlow.getStep(this.position);
    if (_step && _step.stepKind === step.stepKind) {
      step = _step;
    }
    this.currentFlow.events.emit({
      kind: 'integration-set-step',
      position: this.position,
      step: step,
      onSave: () => {
        this.router.navigate(['step-configure', this.position], {
          relativeTo: this.route.parent
        });
      }
    });
  }

  ngOnInit() {
    this.steps = this.stepStore.getSteps();
    this.route.paramMap.first(params => params.has('position'))
      .subscribe(params => {
        this.position = +params.get('position');
        this.currentFlow.events.emit({
          kind: 'integration-step-select',
          position: this.position
        });
      });
  }

  ngOnDestroy() {
    super.ngOnDestroy();
  }
}
