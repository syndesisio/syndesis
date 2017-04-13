import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { FormGroup } from '@angular/forms';
import { DynamicFormControlModel, DynamicFormService } from '@ng2-dynamic-forms/core';

import { Action, Step } from '../../../model';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { FlowPage } from '../flow-page';
import { FormFactoryService } from '../../../common/forms.service';
import { log, getCategory } from '../../../logging';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'ipaas-integrations-action-configure',
  templateUrl: 'action-configure.component.html',
  styleUrls: [ './action-configure.component.scss' ],
})
export class IntegrationsConfigureActionComponent extends FlowPage implements OnInit, OnDestroy {

  routeSubscription: Subscription;
  position: number;
  action: Action = <Action>{};
  step: Step = <Step>{};
  formModel: DynamicFormControlModel[];
  formGroup: FormGroup;
  formConfig: any;

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public formFactory: FormFactoryService,
    public formService: DynamicFormService,
    public detector: ChangeDetectorRef,
  ) {
    super(currentFlow, route, router, detector);
  }

  goBack() {
    const step = this.currentFlow.getStep(this.position);
    step.action = undefined;
    super.goBack(['action-select', this.position]);
  }

  continue(data: any = undefined) {
    if (!data) {
      data = this.formGroup.value || {};
    }
    this.currentFlow.events.emit({
      kind: 'integration-set-properties',
      position: this.position,
      properties: data,
      onSave: () => {
        this.router.navigate(['save-or-add-step'], { queryParams: { validate: true }, relativeTo: this.route.parent });
      },
    });

  }

  ngOnInit() {
    this.routeSubscription = this.route.params.pluck<Params, string>('position')
      .map((position: string) => {
        if (this.position !== undefined) {
          return;
        }
        this.position = Number.parseInt(position);
        const step = <Step> this.currentFlow.getStep(this.position);
        if (!step) {
          this.router.navigate(['connection-select', this.position], { relativeTo: this.route.parent });
          return;
        }
        this.action = step.action;
        if (this.action && this.action.properties) {
          this.formConfig = JSON.parse(JSON.stringify(this.action.properties));
          if (step.configuredProperties) {
            for (const key in <any>step.configuredProperties) {
              if (!step.configuredProperties.hasOwnProperty(key)) {
                continue;
              }
              this.formConfig[key]['value'] = step.configuredProperties[key];
            }
          }
          if (!Object.keys(this.formConfig).length) {
            this.continue({});
            return;
          }
          //log.debugc(() => 'Form config: ' + JSON.stringify(this.formConfig, undefined, 2), category);
          this.formModel = this.formFactory.createFormModel(this.formConfig);
          //log.debugc(() => 'Form model: ' + JSON.stringify(this.formModel, undefined, 2), category);
          this.formGroup = this.formService.createFormGroup(this.formModel);
          setTimeout(() => {
            this.detector.detectChanges();
          }, 30);
        } else {
          this.router.navigate(['action-select', this.position], { relativeTo: this.route.parent });
          return;
        }
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
