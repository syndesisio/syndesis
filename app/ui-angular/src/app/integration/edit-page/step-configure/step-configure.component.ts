import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { FormGroup } from '@angular/forms';
import {
  DynamicFormControlModel,
  DynamicFormService,
} from '@ng-dynamic-forms/core';

import { DataShape, FormFactoryService, Step } from '@syndesis/ui/platform';
import { StepStore, DATA_MAPPER, BASIC_FILTER } from '@syndesis/ui/store';
import {
  CurrentFlowService,
  FlowEvent,
  FlowPageService,
  INTEGRATION_UPDATED,
  INTEGRATION_SET_METADATA,
  INTEGRATION_SET_PROPERTIES,
  INTEGRATION_CANCEL_CLICKED,
  INTEGRATION_DONE_CLICKED,
} from '@syndesis/ui/integration/edit-page';
import {
  INTEGRATION_BUTTON_ENABLE_DONE,
  INTEGRATION_BUTTON_DISABLE_DONE,
} from '../edit-page.models';

@Component({
  selector: 'syndesis-integration-step-configure',
  templateUrl: './step-configure.component.html',
  styleUrls: [
    '../../integration-common.scss',
    './step-configure.component.scss',
  ],
})
export class IntegrationStepConfigureComponent implements OnInit, OnDestroy {
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
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router,
    public formFactory: FormFactoryService,
    public formService: DynamicFormService,
    public stepStore: StepStore
  ) {}

  isInvalidInput() {
    if (this.formGroup) {
      return !this.formGroup.valid;
    }
    return !this.valid;
  }

  cancel() {
    this.flowPageService.maybeRemoveStep(
      this.router,
      this.route,
      this.position
    );
  }

  continue(data: any = {}) {
    const step = this.step;
    if (this.stepStore.isCustomStep(step)) {
      if (step.stepKind === DATA_MAPPER) {
        this.customProperties = {
          atlasmapping: this.mappings,
        };
      }
      data = this.customProperties;
    } else {
      data = this.formGroup ? this.formGroup.value : {};
    }
    this.saveProperties(data, () => {
      this.router.navigate(['save-or-add-step'], {
        queryParams: { validate: true },
        relativeTo: this.route.parent,
      });
    });
  }

  saveProperties(data: any = {}, then?: () => void) {
    const properties = this.formFactory.sanitizeValues(
      { ...data },
      this.formConfig
    );
    this.currentFlowService.events.emit({
      kind: INTEGRATION_SET_PROPERTIES,
      position: this.position,
      properties,
      onSave: () => {
        // flag that this step is configured too for consistency
        // with how connections are configured
        this.currentFlowService.events.emit({
          kind: INTEGRATION_SET_METADATA,
          position: this.position,
          metadata: { configured: 'true' },
          onSave: () => {
            if (then) {
              then();
            }
          },
        });
      },
    });
  }

  setMappings(mappings: string) {
    this.mappings = mappings;
    if (typeof this.mappings !== 'undefined') {
      this.currentFlowService.events.emit({
        kind: INTEGRATION_BUTTON_ENABLE_DONE,
      });
    } else {
      this.currentFlowService.events.emit({
        kind: INTEGRATION_BUTTON_DISABLE_DONE,
      });
    }
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
    const step = (this.step = <Step>(
      this.currentFlowService.getStep(this.position)
    ));
    // If no Step exists redirect to the Select Step view
    if (!step || step.stepKind === 'endpoint') {
      this.router.navigate(['step-select', this.position], {
        relativeTo: this.route.parent,
      });
      return;
    }
    // If there's a step but it's actually an endpoint, redirect to the action select view
    if (step.stepKind === 'endpoint') {
      this.router.navigate(['action-select', this.position], {
        relativeTo: this.route.parent,
      });
      return;
    }
    try {
      const prevStep = this.currentFlowService.getPreviousStepWithDataShape(
        this.position
      );
      this.dataShape = prevStep.action.descriptor.outputDataShape;
    } catch (err) {
      // no datashape is available, not a fatal problem in general
    }
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
      case INTEGRATION_UPDATED:
        this.loadForm();
        break;
      case INTEGRATION_CANCEL_CLICKED:
        this.cancel();
        break;
      case INTEGRATION_DONE_CLICKED:
        this.continue();
        break;
      default:
        break;
    }
  }

  ngOnInit() {
    this.flowSubscription = this.currentFlowService.events.subscribe(
      (event: FlowEvent) => {
        this.handleFlowEvent(event);
      }
    );
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
      setTimeout(() => {
        // defer executing this until the view handles the above changes
        this.loadForm();
      }, 1);
    });
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
