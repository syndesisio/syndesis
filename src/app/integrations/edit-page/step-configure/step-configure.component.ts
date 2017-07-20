import {
  Component,
  Input,
  OnInit,
  OnDestroy,
  ChangeDetectorRef,
} from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { FormGroup } from '@angular/forms';
import {
  DynamicFormControlModel,
  DynamicFormService,
} from '@ng2-dynamic-forms/core';

import { FlowPage } from '../flow-page';
import { StepStore } from '../../../store/step/step.store';
import { FormFactoryService } from '../../../common/forms.service';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { Action, Step } from '../../../model';
import { log, getCategory } from '../../../logging';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'syndesis-integrations-step-configure',
  templateUrl: './step-configure.component.html',
  styleUrls: ['./step-configure.component.scss'],
})
export class IntegrationsStepConfigureComponent extends FlowPage
  implements OnInit, OnDestroy {
  routeSubscription: Subscription;
  position: number;
  step: Step = <Step>{};
  formModel: DynamicFormControlModel[];
  formGroup: FormGroup;
  formConfig: any;
  cfg: any = undefined;
  filterForm: any;

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public formFactory: FormFactoryService,
    public formService: DynamicFormService,
    public detector: ChangeDetectorRef,
    public stepStore: StepStore,
  ) {
    super(currentFlow, route, router, detector);
  }

  goBack() {
    const step = this.currentFlow.getStep(this.position);
    step.stepKind = undefined;
    step.configuredProperties = undefined;
    super.goBack(['step-select', this.position]);
  }

  continue(data: any) {
    const step = this.currentFlow.getStep(this.position);
    switch (step.stepKind) {
      case 'mapper':
        this.router.navigate(['save-or-add-step'], {
          queryParams: { validate: true },
          relativeTo: this.route.parent,
        });
        return;
    }
    if (this.filterForm) {
      data = this.filterForm;
      log.debugc(() => 'Filterform: ' + data);
    }
    if (!data) {
      data = this.formGroup.value || {};
    }
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
        this.router.navigate(['save-or-add-step'], {
          queryParams: { validate: true },
          relativeTo: this.route.parent,
        });
      },
    });
  }

  getToolbarClass() {
    switch (this.currentFlow.getStep(this.position).stepKind) {
      case 'basic-filter':
        return 'toolbar basic-filter';
      case 'mapper':
        return 'toolbar mapper';
    }
    return 'toolbar';
  }

  getConfiguredProperties(props: any) {
    if (typeof props === 'string') {
      return JSON.parse(props);
    } else {
      return props;
    }
  }

  ngOnInit() {
    this.routeSubscription = this.route.params
      .pluck<Params, string>('position')
      .map((position: string) => {
        this.position = Number.parseInt(position);
        const step = (this.step = <Step>this.currentFlow.getStep(
          this.position,
        ));
        // If no Step exists, redirect to the Select Step view
        if (!step) {
          this.router.navigate(['step-select', this.position], {
            relativeTo: this.route.parent,
          });
          return;
        }

        // Step exists, get its configuration
        const stepDef = this.stepStore.getStepConfig(step.stepKind);
        log.debugc(() => 'stepConfig: ' + JSON.stringify(stepDef));
        if (!stepDef) {
          // TODO if we don't have a definition for this step then ???
          return;
        }
        // Now check if we've a custom view for this step kind
        switch (step.stepKind) {
          case 'basic-filter':
            this.filterForm = this.getConfiguredProperties(step.configuredProperties || {});
            return;
          case 'mapper':
            log.debugc(
              () =>
                'No form configuration, skipping the form building service..',
            );
            return;
        }
        this.formConfig = JSON.parse(JSON.stringify(stepDef.properties));
        const values: any = this.getConfiguredProperties(
          step.configuredProperties,
        );
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
        if (!Object.keys(this.formConfig).length) {
          this.continue({});
          return;
        }
        log.debugc(
          () => 'Form config: ' + JSON.stringify(this.formConfig, undefined, 2),
          category,
        );

        // Call formService to build the form
        this.formModel = this.formFactory.createFormModel(this.formConfig);
        this.formGroup = this.formService.createFormGroup(this.formModel);

        setTimeout(() => {
          this.detector.detectChanges();
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
