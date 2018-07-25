import {
  Component,
  OnInit,
  OnDestroy,
  AfterViewInit,
  ChangeDetectorRef
} from '@angular/core';
import { Subscription } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { FormGroup } from '@angular/forms';
import {
  DynamicFormControlModel,
  DynamicFormService
} from '@ng-dynamic-forms/core';

import {
  DataShape,
  FormFactoryService,
  Step
} from '@syndesis/ui/platform';
import { StepStore, DATA_MAPPER, BASIC_FILTER } from '@syndesis/ui/store';
import {
  CurrentFlowService,
  FlowEvent,
  FlowPageService
} from '@syndesis/ui/integration/edit-page';

@Component({
  selector: 'syndesis-integration-step-configure',
  templateUrl: './step-configure.component.html',
  styleUrls: [
    '../../integration-common.scss',
    './step-configure.component.scss'
  ]
})
export class IntegrationStepConfigureComponent
  implements OnInit, OnDestroy, AfterViewInit {
  flowSubscription: Subscription;
  position: number;
  step: Step;
  formModel: DynamicFormControlModel[];
  formGroup: FormGroup;
  formConfig: any;
  cfg: any;
  customProperties: any;
  dataShape: DataShape;
  loading = false;
  error: any;
  valid = false;
  routeSubscription: Subscription;
  mappings: string;

  constructor(
    private changeDetectorRef: ChangeDetectorRef,
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router,
    public formFactory: FormFactoryService,
    public formService: DynamicFormService,
    public stepStore: StepStore
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
    const step = this.step;
    if (step.stepKind === DATA_MAPPER) {
      this.customProperties = {
        atlasmapping: this.mappings
      };
    }
    if (this.stepStore.isCustomStep(step)) {
      data = this.customProperties;
    } else {
      data = this.formGroup ? this.formGroup.value : {};
    }
    this.currentFlowService.events.emit({
      kind: 'integration-set-properties',
      position: this.position,
      properties: this.formFactory.sanitizeValues({ ...data }, this.formConfig),
      onSave: () => {
        this.router.navigate(['save-or-add-step'], {
          queryParams: { validate: true },
          relativeTo: this.route.parent
        });
      }
    });
  }

  setMappings(mappings: string) {
    this.mappings = mappings;
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
    const step = (this.step = <Step>this.currentFlowService.getStep(
      this.position
    ));
    // If no Step exists or it's not actually a step, redirect to the Select Step view
    if (!step || step.stepKind === 'endpoint') {
      this.router.navigate(['step-select', this.position], {
        relativeTo: this.route.parent
      });
      return;
    }

    const prevStep = this.currentFlowService.getPreviousStepWithDataShape(
      this.position
    );
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
    this.routeSubscription = this.route.paramMap.subscribe(params => {
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
