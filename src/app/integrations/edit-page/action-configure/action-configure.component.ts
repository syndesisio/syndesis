import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { FormGroup } from '@angular/forms';
import {
  DynamicFormControlModel,
  DynamicFormService,
} from '@ng2-dynamic-forms/core';

import { Action, Step } from '../../../model';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { FlowPage } from '../flow-page';
import { FormFactoryService } from '../../../common/forms.service';
import { log, getCategory } from '../../../logging';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'syndesis-integrations-action-configure',
  templateUrl: 'action-configure.component.html',
  styleUrls: ['./action-configure.component.scss'],
})
export class IntegrationsConfigureActionComponent extends FlowPage
  implements OnInit, OnDestroy {
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
        this.router.navigate(['save-or-add-step'], {
          queryParams: { validate: true },
          relativeTo: this.route.parent,
        });
      },
    });
  }

  ngOnInit() {
    this.routeSubscription = this.route.params
      .pluck<Params, string>('position')
      .map((position: string) => {
        log.infoc(() => 'Rendering action configuration at position ' + position, category);
        this.position = Number.parseInt(position);
        const step = <Step>this.currentFlow.getStep(this.position);
        if (!step) {
          this.router.navigate(['connection-select', this.position], {
            relativeTo: this.route.parent,
          });
          return;
        }
        this.action = step.action;
        this.step = step;
        if (this.action && this.action.definition) {
          this.formConfig = JSON.parse(JSON.stringify(this.action.definition.propertyDefinitionSteps[0].properties));
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
          this.formModel = this.formFactory.createFormModel(this.formConfig);
          this.formGroup = this.formService.createFormGroup(this.formModel);
          setTimeout(() => {
            this.detector.detectChanges();
          }, 30);
          this.currentFlow.events.emit({
            kind: 'integration-action-configure',
            position: this.position,
          });
        } else {
          this.router.navigate(['save-or-add-step'], {
            queryParams: { validate: true },
            relativeTo: this.route.parent,
          });
        }
      })
      .subscribe();
  }

  ngOnDestroy() {
    this.routeSubscription.unsubscribe();
  }
}
