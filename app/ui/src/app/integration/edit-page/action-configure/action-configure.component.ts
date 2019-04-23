import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { FormGroup } from '@angular/forms';
import {
  DynamicFormControlModel,
  DynamicFormService,
} from '@ng-dynamic-forms/core';
import { Subscription } from 'rxjs';

import {
  ActionDescriptor,
  FormFactoryService,
  Action,
  Step,
  IntegrationSupportService,
} from '@syndesis/ui/platform';
import {
  CurrentFlowService,
  FlowPageService,
  INTEGRATION_SET_STEP,
  INTEGRATION_SET_METADATA,
  INTEGRATION_SET_PROPERTIES,
  INTEGRATION_SET_DESCRIPTOR,
  INTEGRATION_CANCEL_CLICKED,
} from '@syndesis/ui/integration/edit-page';

@Component({
  selector: 'syndesis-integration-action-configure',
  templateUrl: 'action-configure.component.html',
  styleUrls: [
    '../../integration-common.scss',
    './action-configure.component.scss',
  ],
})
export class IntegrationConfigureActionComponent implements OnInit, OnDestroy {
  isInputShapeless: boolean;
  isOutputShapeless: boolean;
  routeSubscription: Subscription;
  flowSubscription: Subscription;
  position: number;
  page: number;
  lastPage: number;
  descriptor: any;
  action: Action = <Action>{};
  step: Step = <Step>{};
  formModel: DynamicFormControlModel[];
  formGroup: FormGroup;
  formConfig: any;
  loading: boolean;
  error: any = undefined;
  hasConfiguration = false;

  constructor(
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router,
    public formFactory: FormFactoryService,
    public formService: DynamicFormService,
    public integrationSupport: IntegrationSupportService
  ) {
    // nothing to do
  }

  isApiProvider() {
    return this.step.connection.connectorId === 'api-provider';
  }

  goBack() {
    const { action: omit, ...step } = this.currentFlowService.getStep(
      this.position
    );
    this.currentFlowService.events.emit({
      kind: INTEGRATION_SET_STEP,
      position: this.position,
      step,
      onSave: () =>
        this.flowPageService.goBack(
          ['action-select', this.position],
          this.route
        ),
    });
  }

  buildData(data: any) {
    const formValue = this.formFactory.sanitizeValues(
      this.formGroup ? this.formGroup.value : {},
      this.formConfig
    );
    return { ...this.step.configuredProperties, ...formValue, ...data };
  }

  previous(data: any = undefined) {
    data = this.buildData(data);
    this.currentFlowService.events.emit({
      kind: INTEGRATION_SET_PROPERTIES,
      position: this.position,
      properties: data,
      onSave: () => {
        if (this.page === 0) {
          /* All done configuring this action... */
          this.router.navigate(['save-or-add-step'], {
            queryParams: { validate: true },
            relativeTo: this.route.parent,
          });
        } else {
          /* Go to the previous configuration page... */
          this.router.navigate(
            ['action-configure', this.position, this.page - 1],
            {
              relativeTo: this.route.parent,
            }
          );
        }
      },
    });
  }

  maybeDisableDone() {
    return this.formGroup ? this.formGroup.invalid : false;
  }

  canShowDone() {
    const isLastPage = (this.page >= this.lastPage || !this.hasConfiguration);
    if (!isLastPage) {
      return false;
    }

    if (this.position == 0) {
      // First step - only care about output shape
      return !this.isOutputShapeless;
    }

    const isLastStep = this.currentFlowService.atEnd(this.position);
    if (isLastStep) {
      // Last step - only care about input shape
      return !this.isInputShapeless;
    }

    // Other steps - care about both input and output shapes
    return !this.isInputShapeless && !this.isOutputShapeless;
  }

  canShowNext() {
    const hasMorePages = this.page < this.lastPage;
    if (hasMorePages) {
      return true;
    }

    if (this.position == 0) {
      // First step - only care about output shape
      return this.isOutputShapeless;
    }

    const isLastStep = this.currentFlowService.atEnd(this.position);
    if (isLastStep) {
      // Last step - only care about input shape
      return this.isInputShapeless;
    }

    // Other steps - care about both input and output shapes
    return this.isInputShapeless || this.isOutputShapeless;
  }

  finishUp() {
    let direction = 'output';
    if (this.position > 0) {
      direction = 'input';
    }
    this.currentFlowService.events.emit({
      kind: INTEGRATION_SET_METADATA,
      position: this.position,
      metadata: { configured: 'true' },
      onSave: () => {
        this.router.navigate(['describe-data', this.position, direction], {
          relativeTo: this.route.parent,
        });
      },
    });
  }

  continue() {
    this.loading = true;
    this.error = undefined;
    const configuredProperties = this.buildData({});
    this.currentFlowService.events.emit({
      kind: INTEGRATION_SET_PROPERTIES,
      position: this.position,
      properties: configuredProperties,
      onSave: () => {
        const step = (this.step = this.currentFlowService.getStep(
          this.position
        ));
        if (!this.lastPage || this.page >= this.lastPage) {
          /**
           * If there are action properties that depend on having other action
           * properties defined in previous steps we need to fetch metadata one
           * more time and apply those action property values that we get
           */
          if (Object.keys(step.configuredProperties).length > 0) {
            this.integrationSupport
              .fetchMetadata(step.connection, step.action, configuredProperties)
              .toPromise()
              .then((descriptor: ActionDescriptor) => {
                this.currentFlowService.events.emit({
                  kind: INTEGRATION_SET_DESCRIPTOR,
                  position: this.position,
                  descriptor,
                  onSave: () => {
                    /* All done... */
                    this.finishUp();
                  },
                });
              })
              .catch(error => {
                this.setError(error);
                this.loading = false;
              });
          } else {
            /* All done... */
            this.finishUp();
          }
        } else {
          /* Go to the next wizard page... */
          this.router.navigate(
            ['action-configure', this.position, this.page + 1],
            {
              relativeTo: this.route.parent,
            }
          );
        }
      },
    });
  }

  setError(error) {
    // the message is in the _meta attribute in the response
    const message = error.data._meta ? error.data._meta.message : null;
    this.error = {
      class: 'alert alert-warning',
      icon: 'pficon pficon-warning-triangle-o',
      message: message || error.message || error.userMsg || error.developerMsg,
    };
  }

  preInitialize(position: number, page: number) {
    this.error = undefined;
    const step = this.currentFlowService.getStep(this.position);
    if (!step || !step.connection) {
      this.router.navigate(['step-select', this.position], {
        relativeTo: this.route.parent,
      });
      return;
    }
    if (!step.action) {
      this.router.navigate(['action-select', this.position], {
        relativeTo: this.route.parent,
      });
      return;
    }
    this.action = step.action;
    this.step = step;

    this.integrationSupport
      .fetchMetadata(
        this.step.connection,
        this.step.action,
        this.configuredPropertiesForMetadataCall(step.action)
      )
      .toPromise()
      .then((descriptor: ActionDescriptor) => {
        this.currentFlowService.events.emit({
          kind: INTEGRATION_SET_DESCRIPTOR,
          position: this.position,
          descriptor,
          onSave: () => {
            this.initialize(position, page, descriptor);
          },
        });
      })
      .catch(error => {
        this.initialize(position, page, undefined, error);
      });
  }

  initialize(
    position: number,
    page: number,
    descriptor: ActionDescriptor,
    error?: any
  ) {
    if (error) {
      this.setError(error);
      this.loading = false;
      return;
    }
    this.isInputShapeless = this.currentFlowService.isActionInputShapeless(descriptor);
    this.isOutputShapeless = this.currentFlowService.isActionOutputShapeless(descriptor);
    if (this.hasNoActionPropertiesToDisplay(descriptor)) {
      this.loading = false;
      // TODO figure out how to get a link in here that works
      this.error = {
        class: 'alert alert-info',
        icon: 'pficon pficon-info',
        message: 'There are no properties to configure for this action.',
      };
      const metadata = this.step.metadata || {};
      if (!metadata.configured) {
        this.finishUp();
      }
      return;
    }
    const lastPage = (this.lastPage =
      descriptor.propertyDefinitionSteps.length - 1);
    if (descriptor.propertyDefinitionSteps && page <= lastPage) {
      this.descriptor = JSON.parse(
        JSON.stringify(descriptor.propertyDefinitionSteps[page])
      );
      this.formConfig = this.descriptor.properties;
    } else {
      this.formConfig = {};
    }
    if (!Object.keys(this.formConfig).length) {
      return;
    }
    this.hasConfiguration = true;
    this.formModel = this.formFactory.createFormModel(
      this.formConfig,
      this.step.configuredProperties
    );
    this.formGroup = this.formService.createFormGroup(this.formModel);
    setTimeout(() => {
      this.loading = false;
    }, 30);
  }

  hasNoActionPropertiesToDisplay(descriptor: ActionDescriptor) {
    if (typeof descriptor === 'undefined' ||
      descriptor.propertyDefinitionSteps === undefined ||
      Object.keys(descriptor.propertyDefinitionSteps[0]).length === 0 ||
      Object.keys(descriptor.propertyDefinitionSteps[0].properties).length === 0) {
      return true;
    }

    return Object.keys(descriptor.propertyDefinitionSteps[0].properties).filter(key => {
      return descriptor.propertyDefinitionSteps[0].properties[key].type != 'hidden';
    }).length === 0;
  }

  configuredPropertiesForMetadataCall(action: Action) {
    /**
     * Fetches all properties from 0..this.position-1 steps, this way
     * the backend does not fix a value and drills down into the detail
     * of the selected value in this.position step, this allows enum
     * values to be present in the same way as they are present when no
     * value is selected by the user
     */
    const props = {};
    if (!action) {
      return props;
    }
    const stepDefinitions = action.descriptor.propertyDefinitionSteps;
    for (let p = 0; p < this.page; p++) {
      for (const prop in stepDefinitions[p].properties) {
        /* We don't want null or undefined values here */
        if (this.step.configuredProperties[prop] != null) {
          props[prop] = this.step.configuredProperties[prop];
        }
      }
    }
    return props;
  }

  ngOnInit() {
    this.hasConfiguration = false;
    this.flowSubscription = this.currentFlowService.events.subscribe(event => {
      if (event.kind === INTEGRATION_CANCEL_CLICKED) {
        this.flowPageService.maybeRemoveStep(
          this.router,
          this.route,
          this.position
        );
      }
    });
    this.routeSubscription = this.route.paramMap.subscribe(
      (params: ParamMap) => {
        if (!params.has('page')) {
          this.router.navigate(['0'], {
            relativeTo: this.route,
          });
          return;
        }
        const page = (this.page = Number.parseInt(params.get('page')));
        const position = (this.position = Number.parseInt(
          params.get('position')
        ));
        this.loading = true;
        this.preInitialize(position, page);
      }
    );
  }

  ngOnDestroy() {
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
    if (this.flowSubscription) {
      this.flowSubscription.unsubscribe();
    }
  }
}
