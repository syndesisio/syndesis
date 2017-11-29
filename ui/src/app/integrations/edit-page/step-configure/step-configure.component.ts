import {
  Component,
  Input,
  OnInit,
  OnDestroy,
} from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { FormGroup } from '@angular/forms';
import { DynamicFormControlModel, DynamicFormService } from '@ng-dynamic-forms/core';

import { FlowPage } from '../flow-page';
import {
  StepStore,
  DATA_MAPPER,
  BASIC_FILTER
} from '../../../store/step/step.store';
import { FormFactoryService } from '../../../common/forms.service';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { Action, Step, DataShape } from '../../../model';
import { IntegrationSupportService } from '../../../store/integration-support.service';
import { log, getCategory } from '../../../logging';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'syndesis-integrations-step-configure',
  templateUrl: './step-configure.component.html',
  styleUrls: ['./step-configure.component.scss']
})
export class IntegrationsStepConfigureComponent extends FlowPage implements OnInit, OnDestroy {
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

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public formFactory: FormFactoryService,
    public formService: DynamicFormService,
    public stepStore: StepStore,
    public integrationSupport: IntegrationSupportService
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

  fetchDataShapesFor(step: Step, output = true) {
    return this.integrationSupport
      .fetchMetadata(
      step.connection,
      step.action,
      step.configuredProperties || {}
      )
      .toPromise()
      .then(response => {
        log.debug('Response: ' + JSON.stringify(response, undefined, 2));
        const definition: any = response['_body']
          ? JSON.parse(response['_body'])
          : undefined;
        if (output) {
          this.outputDataShape = definition.outputDataShape;
        } else {
          this.inputDataShape = definition.inputDataShape;
        }
      })
      .catch(response => {
        this.loading = false;
        const error = JSON.parse(response['_body']);
        this.error = {
          class: 'alert alert-warning',
          message: error.message || error.userMsg || error.developerMsg
        };
        log.info(
          'Error fetching data shape for ' +
          JSON.stringify(step) +
          ' : ' +
          JSON.stringify(response)
        );
      });
  }

  loadForm() {
    if (!this.currentFlow.loaded) {
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

    // we want the output shape of the previous connection
    const prevConnection = this.currentFlow.getPreviousConnection(
      this.position
    );
    // we want the input shape of the next connection
    const nextConnection = this.currentFlow.getSubsequentConnection(
      this.position
    );

    this.fetchDataShapesFor(prevConnection, true)
      .then(() => this.fetchDataShapesFor(nextConnection, false))
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
    log.info(
      'Form config: ' + JSON.stringify(this.formConfig, undefined, 2),
      category
    );

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
    this.route.paramMap.first(params => params.has('position'))
      .subscribe(params => {
        this.position = +params.get('position');
        this.loadForm();
      });
  }

  ngOnDestroy() {
    super.ngOnDestroy();
  }
}
