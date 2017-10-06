import {
  Component,
  Input,
  OnInit,
  OnDestroy,
  ChangeDetectorRef,
} from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { FormGroup } from '@angular/forms';
import {
  DynamicFormControlModel,
  DynamicFormService,
} from '@ng2-dynamic-forms/core';

import { FlowPage } from '../flow-page';
import {
  StepStore,
  DATA_MAPPER,
  BASIC_FILTER,
} from '../../../store/step/step.store';
import { FormFactoryService } from '../../../common/forms.service';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { Action, Step } from '../../../model';
import { IntegrationSupportService } from '../../../store/integration-support.service';
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
  step: Step = undefined;
  formModel: DynamicFormControlModel[] = undefined;
  formGroup: FormGroup = undefined;
  formConfig: any;
  cfg: any = undefined;
  customProperties: any = undefined;

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public formFactory: FormFactoryService,
    public formService: DynamicFormService,
    public detector: ChangeDetectorRef,
    public stepStore: StepStore,
    public integrationSupport: IntegrationSupportService,
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
    // Question: Why is data a parameter here ?
    const step = this.step;

    if (step.stepKind === DATA_MAPPER) {
      this.router.navigate(['save-or-add-step'], {
        queryParams: { validate: true },
        relativeTo: this.route.parent,
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
          relativeTo: this.route.parent,
        });
      },
    });
  }

  getToolbarClass() {
    const stepKind = this.step ? this.step.stepKind : '';
    switch (stepKind) {
      case BASIC_FILTER:
        return 'toolbar basic-filter';
      case DATA_MAPPER:
        return 'toolbar mapper';
    }
    return 'toolbar';
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
    try {
      this.detector.detectChanges();
    } catch (err) {}
    this.currentFlow.events.emit({
      kind: 'integration-action-configure',
      position: this.position,
    });
  }

  fetchDataShapesFor(step: Step) {
    return this.integrationSupport.fetchMetadata(
      step.connection,
      step.action,
      step.configuredProperties || {},
    )
    .toPromise()
    .then(response => {
      log.debug('Response: ' + JSON.stringify(response, undefined, 2));
      const definition: any = response['_body'] ? JSON.parse(response['_body']) : undefined;
      step.action.inputDataShape = definition.inputDataShape;
      step.action.outputDataShape = definition.outputDataShape;
    })
    .catch(response => {
      // TODO error handling
    });
  }

  loadForm() {
    if (!this.currentFlow.loaded) {
      return;
    }
    const step = (this.step = <Step>this.currentFlow.getStep(this.position));
    // If no Step exists, redirect to the Select Step view
    if (!step) {
      this.router.navigate(['step-select', this.position], {
        relativeTo: this.route.parent,
      });
      return;
    }

    if (this.step.stepKind === DATA_MAPPER) {
      // TODO fetch shapes in parallel?
      this.fetchDataShapesFor(this.currentFlow.getStartStep())
      .then(() => this.fetchDataShapesFor(this.currentFlow.getEndStep()))
      .then(() => this.loadFormSetup(step));
    } else {
      this.loadFormSetup(step);
    }
  }

  loadFormSetup(step: Step) {
    // Step exists, get its configuration
    const stepDef = this.stepStore.getStepConfig(step.stepKind);
    log.info('stepConfig: ' + JSON.stringify(stepDef));
    if (!stepDef) {
      // TODO if we don't have a definition for this step then ???
      this.postEvent();
      return;
    }
    // Now check if we've a custom view for this step kind
    if (this.stepStore.isCustomStep(step)) {
      this.customProperties = this.getConfiguredProperties(
        step.configuredProperties || {},
      );
      this.postEvent();
      return;
    }
    this.formConfig = JSON.parse(JSON.stringify(stepDef.properties));
    if (!Object.keys(this.formConfig).length) {
      this.continue({});
      return;
    }
    const values: any = this.getConfiguredProperties(step.configuredProperties);
    log.info(
      'Form config: ' + JSON.stringify(this.formConfig, undefined, 2),
      category,
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
    }
  }

  ngOnInit() {
    this.routeSubscription = this.route.paramMap.subscribe(
      (paramMap: ParamMap) => {
        this.position = +paramMap.get('position');
        this.loadForm();
      },
    );
  }

  ngOnDestroy() {
    this.routeSubscription.unsubscribe();
  }
}
