import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { FormGroup } from '@angular/forms';
import { DynamicFormControlModel, DynamicFormService } from '@ng-dynamic-forms/core';

import { Action, DataShape, FormFactoryService, IntegrationSupportService, Step } from '@syndesis/ui/platform';
import { log, getCategory } from '@syndesis/ui/logging';
import { StepStore, DATA_MAPPER, BASIC_FILTER } from '@syndesis/ui/store';
import { CurrentFlow, FlowEvent, FlowPage } from '@syndesis/ui/integration/edit-page';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'syndesis-integration-step-configure',
  templateUrl: './step-configure.component.html',
  styleUrls: ['./step-configure.component.scss']
})
export class IntegrationStepConfigureComponent extends FlowPage implements OnInit, OnDestroy {
  position: number;
  step: Step = undefined;
  formModel: DynamicFormControlModel[] = undefined;
  formGroup: FormGroup = undefined;
  formConfig: any;
  cfg: any = undefined;
  customProperties: any = undefined;
  // this is the output of the previous step
  outputDataShape: DataShape = undefined;
  // This is the input of the next step
  inputDataShape: DataShape = undefined;
  loading = false;
  error: any = undefined;
  valid = true;
  routeSubscription: Subscription;

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public formFactory: FormFactoryService,
    public formService: DynamicFormService,
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
    this.currentFlow.events.emit({
      kind: 'integration-set-properties',
      position: this.position,
      properties: properties,
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

  postEvent() {
    this.loading = false;
    this.currentFlow.events.emit({
      kind: 'integration-action-configure',
      position: this.position
    });
  }

  loadForm() {
    if (!this.currentFlow.loaded || this.loading) {
      return;
    }
    this.loading = true;
    const step = (this.step = <Step>this.currentFlow.getStep(this.position));
    // If no Step exists or it's not actually a step, redirect to the Select Step view
    if (!step || step.stepKind === 'endpoint') {
      this.router.navigate(['step-select', this.position], {
        relativeTo: this.route.parent
      });
      return;
    }
    const prevStep = this.currentFlow.getPreviousStepWithDataShape(this.position);
    const nextStep = this.currentFlow.getSubsequentStepWithDataShape(this.position);
    this.currentFlow.fetchDataShapeFor(prevStep, true)
      .then(inDataShape => {
        this.inputDataShape = inDataShape;
        this.currentFlow.fetchDataShapeFor(nextStep, false)
          .then(outDataShape => {
            this.outputDataShape = outDataShape;
          })
          .catch(response => this.handleDataShapeError(nextStep, response));
      })
      .catch(response => this.handleDataShapeError(prevStep, response))
      .then(() => this.loadFormSetup(step));
  }

  loadFormSetup(step: Step) {
    // Now check if we've a custom view for this step kind
    if (this.stepStore.isCustomStep(step)) {
      this.customProperties = this.getConfiguredProperties(
        step.configuredProperties || {}
      );
      this.postEvent();
      return;
    }
    this.formConfig = this.stepStore.getProperties(step);
    if (!Object.keys(this.formConfig).length) {
      this.continue({});
      return;
    }
    const values: any = this.getConfiguredProperties(step.configuredProperties);
    /*
    log.info(
      'Form config: ' + JSON.stringify(this.formConfig, undefined, 2),
      category
    );
    */
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
    this.postEvent();
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
        this.inputDataShape = undefined;
        this.outputDataShape = undefined;
        this.error = undefined;
        this.position = +params.get('position');
        this.loadForm();
      });
  }

  ngOnDestroy() {
    super.ngOnDestroy();
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
  }

  private handleDataShapeError(step: Step, response: any) {
    this.loading = false;
    const error = JSON.parse(response['_body']);
    const errorMessage =
      error ? error.message || error.userMsg || error.developerMsg : response;
    this.error = {
      class: 'alert alert-warning',
      message: errorMessage
    };
    log.info(
      'Error fetching data shape for ' +
      JSON.stringify(step) +
      ' : ' +
      JSON.stringify(response)
    );
  }

}
