import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { FormGroup } from '@angular/forms';
import { DynamicFormControlModel, DynamicFormService } from '@ng2-dynamic-forms/core';

import { FlowPage } from '../flow-page';
import { StepStore } from '../../../store/step/step.store';
import { FormFactoryService } from '../../../common/forms.service';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { Action, Step } from '../../../model';
import { log, getCategory } from '../../../logging';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'ipaas-integrations-step-configure',
  templateUrl: './step-configure.component.html',
  styleUrls: ['./step-configure.component.scss'],
})
export class IntegrationsStepConfigureComponent extends FlowPage implements OnInit, OnDestroy {

  routeSubscription: Subscription;
  position: number;
  step: Step = <Step>{};
  formModel: DynamicFormControlModel[];
  formGroup: FormGroup;
  formConfig: any;
  cfg: any = undefined;

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public formFactory: FormFactoryService,
    public formService: DynamicFormService,
    public changeDetectorRef: ChangeDetectorRef,
    public stepStore: StepStore,
  ) {
    super(currentFlow, route, router);
  }

  goBack() {
    const step = this.currentFlow.getStep(this.position);
    step.stepKind = undefined;
    step.configuredProperties = undefined;
    super.goBack(['step-select', this.position]);
  }

  continue() {
    const step = this.currentFlow.getStep(this.position);
    if (step.stepKind === 'mapper') {
      this.router.navigate(['save-or-add-step'], { queryParams: { validate: true }, relativeTo: this.route.parent });
      return;
    }
    const data = this.formGroup.value;
    const properties = {};
    for (const key in data) {
      if (!data.hasOwnProperty(key)) {
        continue;
      }
      properties[key] = data[key];
    }
    this.currentFlow.events.emit({
      kind: 'integration-set-properties',
      position: this.position,
      properties: properties,
      onSave: () => {
        this.router.navigate(['save-or-add-step'], { queryParams: { validate: true }, relativeTo: this.route.parent });
      },
    });
  }

  getConfiguredProperties(props: any) {
    if (typeof props === 'string') {
      return JSON.parse(props);
    } else {
      return props;
    }
  }

  ngOnInit() {
    this.routeSubscription = this.route.params.pluck<Params, string>('position')
      .map((position: string) => {
        this.position = Number.parseInt(position);
        const step = this.step = <Step>this.currentFlow.getStep(this.position);
        if (!step) {
          this.router.navigate(['step-select', this.position], { relativeTo: this.route.parent });
          return;
        }
        const stepDef = this.stepStore.getStepConfig(step.stepKind);
        if (!stepDef) {
          // TODO if we don't have a definition for this step then ???
          return;
        }
        this.formConfig = JSON.parse(JSON.stringify(stepDef.properties));
        const values: any = this.getConfiguredProperties(step.configuredProperties);
        for (const key in values) {
          if (!values.hasOwnProperty(key)) {
            continue;
          }
          // TODO hack to handle an unconfigured step
          const value = values[key];
          if (typeof value === 'object') {
            continue;
          }
          const item = this.formConfig[key];
          if (item) {
            item.value = value;
          }
        }
        log.debugc(() => 'Form config: ' + JSON.stringify(this.formConfig, undefined, 2), category);
        this.formModel = this.formFactory.createFormModel(this.formConfig);
        this.formGroup = this.formService.createFormGroup(this.formModel);
        setTimeout(() => {
          this.changeDetectorRef.detectChanges();
        }, 30);
        this.currentFlow.events.emit({
          kind: 'integration-action-configure',
          position: this.position,
        });
      })
      .subscribe();
  }

  ngOnDestroy() {
    this.routeSubscription.unsubscribe();
  }
}
