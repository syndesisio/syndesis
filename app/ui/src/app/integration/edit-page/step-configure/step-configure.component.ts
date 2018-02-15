import { Component, Input, OnInit, OnDestroy, AfterViewInit, ChangeDetectorRef } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { FormGroup } from '@angular/forms';
import { DynamicFormControlModel, DynamicFormService } from '@ng-dynamic-forms/core';

import { Action, DataShape, FormFactoryService, IntegrationSupportService, Step } from '@syndesis/ui/platform';
import { log, getCategory } from '@syndesis/ui/logging';
import { StepStore, DATA_MAPPER, BASIC_FILTER } from '@syndesis/ui/store';
import { CurrentFlowService, FlowEvent, FlowPageService } from '@syndesis/ui/integration/edit-page';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'syndesis-integration-step-configure',
  templateUrl: './step-configure.component.html',
  styleUrls: ['./step-configure.component.scss']
})
export class IntegrationStepConfigureComponent implements OnInit, OnDestroy, AfterViewInit {
  flowSubscription: Subscription;
  position: number;
  step: Step = undefined;
  formModel: DynamicFormControlModel[] = undefined;
  formGroup: FormGroup = undefined;
  formConfig: any;
  cfg: any = undefined;
  customProperties: any = undefined;
  dataShape: DataShape = undefined;
  loading = false;
  error: any = undefined;
  valid = true;
  routeSubscription: Subscription;

  constructor(
    private changeDetectorRef: ChangeDetectorRef,
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router,
    public formFactory: FormFactoryService,
    public formService: DynamicFormService,
    public stepStore: StepStore,
  ) {
    this.flowSubscription = this.currentFlowService.events.subscribe(
      (event: FlowEvent) => {
        this.handleFlowEvent(event);
      }
    );
  }

  goBack() {
    const step = this.currentFlowService.getStep(this.position);
    step.stepKind = undefined;
    step.configuredProperties = undefined;
    this.flowPageService.goBack(['step-select', this.position], this.route);
  }

  isInvalidInput() {
    if (this.formGroup) {
      return !this.formGroup.valid;
    }
    return !this.valid;
  }

  continue(data: any) {
    // Question: Why is data a parameter here ?
    const step = this.step;

    if (step.stepKind === DATA_MAPPER) {
      this.router.navigate(['save-or-add-step'], {
        queryParams: { validate: true },
        relativeTo: this.route.parent
      });
      return;
    }

    if (this.stepStore.isCustomStep(step)) {
      data = this.customProperties;
    } else {
      data = this.formGroup ? this.formGroup.value : {};
    }

    // set a copy in the integration
    const properties = JSON.parse(JSON.stringify(data));
    this.currentFlowService.events.emit({
      kind: 'integration-set-properties',
      position: this.position,
      properties: data,
      onSave: () => {
        this.router.navigate(['save-or-add-step'], {
          queryParams: { validate: true },
          relativeTo: this.route.parent
        });
      }
    });
  }

  getToolbarClass() {
    const stepKind = this.step ? this.step.stepKind : '';
    switch (stepKind) {
      case BASIC_FILTER:
        return 'toolbar basic-filter';
      case DATA_MAPPER:
        return 'toolbar mapper';
      default:
        return 'toolbar';
    }
  }

  getConfiguredProperties(props: any) {
    if (props === undefined) {
      return undefined;
    }
    if (typeof props === 'string') {
      return JSON.parse(props);
    } else {
      return JSON.parse(JSON.stringify(props));
    }
  }

  loadForm() {
    if (!this.currentFlowService.loaded || this.loading) {
      return;
    }
    this.loading = true;
    const step = (this.step = <Step>this.currentFlowService.getStep(this.position));
    // If no Step exists or it's not actually a step, redirect to the Select Step view
    if (!step || step.stepKind === 'endpoint') {
      this.router.navigate(['step-select', this.position], {
        relativeTo: this.route.parent
      });
      return;
    }

    const prevStep = this.currentFlowService.getPreviousStepWithDataShape(this.position);
    this.dataShape = prevStep.action.descriptor.outputDataShape;
    this.loadFormSetup(this.step);
  }

  loadFormSetup(step: Step) {
    // Now check if we've a custom view for this step kind
    if (this.stepStore.isCustomStep(step)) {
      this.customProperties = this.getConfiguredProperties(
        step.configuredProperties || {}
      );
      this.loading = false;
      return;
    }
    this.formConfig = this.stepStore.getProperties(step);
    if (!Object.keys(this.formConfig).length) {
      this.continue({});
      return;
    }
    const values: any = this.getConfiguredProperties(step.configuredProperties);
    if (values) {
      // supress null values
      for (const key in values) {
        if (values[key] === 'null') {
          values[key] = undefined;
        }
      }
    }
    // Call formService to build the form
    this.formModel = this.formFactory.createFormModel(this.formConfig, values);
    this.formGroup = this.formService.createFormGroup(this.formModel);
    this.loading = false;
  }

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case 'integration-updated':
        this.loadForm();
        break;
      default:
        break;
    }
  }

  ngOnInit() {
    this.routeSubscription = this.route.paramMap
      .subscribe(params => {
        /* totally reset our state just to be safe*/
        this.loading = false;
        this.step = undefined;
        this.formModel = undefined;
        this.formGroup = undefined;
        this.formConfig = undefined;
        this.cfg = undefined;
        this.customProperties = undefined;
        this.dataShape = undefined;
        this.error = undefined;
        this.position = +params.get('position');
        this.loadForm();
      });
  }

  ngAfterViewInit() {
    this.changeDetectorRef.detectChanges();
  }

  ngOnDestroy() {
    if (this.flowSubscription) {
      this.flowSubscription.unsubscribe();
    }
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
  }
}
