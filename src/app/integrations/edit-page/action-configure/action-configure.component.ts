import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
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
  page: number;
  definition: any;
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
    // TODO - actually deal with multi-step forms
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

  initialize(position: number, page: number) {
    const step = <Step>this.currentFlow.getStep(this.position);
    if (!step) {
      this.router.navigate(['connection-select', this.position], {
        relativeTo: this.route.parent,
      });
      return;
    }
    this.action = step.action;
    this.step = step;
    if (!this.action || !this.action.definition) {
      this.router.navigate(['save-or-add-step'], {
        queryParams: { validate: true },
        relativeTo: this.route.parent,
      });
      return;
    }
    const definition = this.action.definition;
    if (
      definition &&
      definition.propertyDefinitionSteps &&
      page < definition.propertyDefinitionSteps.length
    ) {
      this.definition = JSON.parse(
        JSON.stringify(definition.propertyDefinitionSteps[page]),
      );
      this.formConfig = this.definition.properties;
    } else {
      this.formConfig = {};
    }
    if (!Object.keys(this.formConfig).length) {
      // No configuration, store an empty value and move along to the next page...
      this.continue({});
      return;
    }
    this.formModel = this.formFactory.createFormModel(
      this.formConfig,
      step.configuredProperties,
    );
    this.formGroup = this.formService.createFormGroup(this.formModel);
    setTimeout(() => {
      try {
        this.currentFlow.events.emit({
          kind: 'integration-action-configure',
          position: this.position,
        });
        this.detector.detectChanges();
      } catch (err) {}
    }, 30);
  }

  ngOnInit() {
    this.routeSubscription = this.route.paramMap.subscribe(
      (params: ParamMap) => {
        if (!params.has('page')) {
          this.router.navigate(['0'], { relativeTo: this.route });
          return;
        }
        const page = (this.page = Number.parseInt(params.get('page')));
        const position = (this.position = Number.parseInt(
          params.get('position'),
        ));
        this.initialize(position, page);
      },
    );
  }

  ngOnDestroy() {
    this.routeSubscription.unsubscribe();
  }
}
