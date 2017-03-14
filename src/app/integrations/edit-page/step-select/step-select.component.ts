import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';

import { StepStore } from '../../../store/step/step.store';
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
  ) {
    super(currentFlow, route, router);
    this.steps = stepStore.getSteps();
  }

  goBack() {
    super.goBack(['save-or-add-step']);
  }

  getName(step: Step) {
    switch (step.stepKind) {
      case 'log':
        return 'Log';
      case 'datamapper':
        return 'Data Mapper';
    }
  }

  getDescription(step: Step) {
    switch (step.stepKind) {
      case 'log':
        return 'Sends a message to the integration\'s log';
      case 'datamapper':
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
