import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { FormGroup } from '@angular/forms';
import { DynamicFormControlModel, DynamicFormService } from '@ng2-dynamic-forms/core';

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
export class IntegrationsStepConfigureComponent implements OnInit, OnDestroy {

  flowSubscription: Subscription;
  routeSubscription: Subscription;
  position: number;
  action: Action = <Action>{};
  step: Step = <Step>{};
  formModel: DynamicFormControlModel[];
  formGroup: FormGroup;
  formConfig: any;

  public html:string = `<span class="btn btn-danger">Never trust not sanitized HTML!!!</span>`;

  constructor(
    private currentFlow: CurrentFlow,
    private route: ActivatedRoute,
    private router: Router,
    private formFactory: FormFactoryService,
    private formService: DynamicFormService,
    private changeDetectorRef: ChangeDetectorRef,
  ) { }

  cancel() {
    this.router.navigate(['integrations']);
  }

  goBack() {
    this.router.navigate(['step-select', this.position], { relativeTo: this.route.parent });
  }

  continue() {
    const data = this.formGroup.value;
    for (const key in data) {
      if (!data.hasOwnProperty(key)) {
        continue;
      }
      this.formConfig[key].value = data[key];
    }
    this.currentFlow.events.emit({
      kind: 'integration-set-properties',
      position: this.position,
      properties: JSON.stringify(this.formConfig),
      onSave: () => {
        this.router.navigate(['save-or-add-step'], { queryParams: { validate: true }, relativeTo: this.route.parent });
      },
    });
  }

  ngOnInit() {
    this.routeSubscription = this.route.params.pluck<Params, string>('position')
      .map((position: string) => {
        this.position = Number.parseInt(position);
        const step = this.step = <Step>this.currentFlow.getStep(this.position);
        if (!step || !step.configuredProperties) {
          this.router.navigate(['step-select', this.position], { relativeTo: this.route.parent });
          return;
        }
        this.action = step.action;
        const configString = step.configuredProperties;
        this.formConfig = undefined;
        try {
          this.formConfig = JSON.parse(configString);
        } catch (err) {
          log.debugc(() => 'Error parsing form config', category);
        }
        log.debugc(() => 'Form config: ' + JSON.stringify(this.formConfig, undefined, 2), category);
        this.formModel = this.formFactory.createFormModel(this.formConfig);
        log.debugc(() => 'Form model: ' + JSON.stringify(this.formModel, undefined, 2), category);
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
