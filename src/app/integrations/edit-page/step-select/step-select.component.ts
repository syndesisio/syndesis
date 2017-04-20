import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import { StepStore, StepKind, StepKinds } from '../../../store/step/step.store';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { FlowPage } from '../flow-page';
import { Step, Steps, TypeFactory } from '../../../model';

@Component({
  selector: 'ipaas-integrations-step-select',
  templateUrl: './step-select.component.html',
  styleUrls: ['./step-select.component.scss'],
})
export class IntegrationsStepSelectComponent extends FlowPage implements OnInit {

  steps: Steps;
  routeSubscription: Subscription;
  position: number;

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public stepStore: StepStore,
    public detector: ChangeDetectorRef,
  ) {
    super(currentFlow, route, router, detector);
    this.steps = stepStore.getSteps();
  }

  goBack() {
    super.goBack(['save-or-add-step']);
  }

  getName(step: StepKind) {
    // TODO this should be the norm
    if (step.name) {
      return step.name;
    }
    // fallback
    switch (step.stepKind) {
      case 'log':
        return 'Log';
      case 'mapper':
        return 'Data Mapper';
      default:
        // TODO not ideal
        return step.stepKind;
    }
  }

  getDescription(step: StepKind) {
    if (step.description) {
      return step.description;
    }
    switch (step.stepKind) {
      case 'log':
        return 'Sends a message to the integration\'s log';
      case 'mapper':
        return 'Map fields from the input type to the output type';
    }
  }

  isSelected(step: Step) {
    const _step = this.currentFlow.getStep(this.position);
    return _step && step.stepKind === _step.stepKind;
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
        this.router.navigate(['step-configure', this.position], { relativeTo: this.route.parent });
      },
    });
  }

  ngOnInit() {
    this.routeSubscription = this.route.params.pluck<Params, string>('position')
      .map((position: string) => {
        this.position = Number.parseInt(position);
        this.currentFlow.events.emit({
          kind: 'integration-step-select',
          position: this.position,
        });
      })
      .subscribe();
   }
}
