import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, ParamMap, Router } from '@angular/router';
import { FormGroup } from '@angular/forms';
import {
  DynamicFormControlModel,
  DynamicFormService
} from '@ng-dynamic-forms/core';

import { IntegrationSupportService } from '@syndesis/ui/store';
import { Action, Step } from '@syndesis/ui/model';
import { CurrentFlow, FlowPage } from '@syndesis/ui/integrations/edit-page';
import { FormFactoryService, UserService } from '@syndesis/ui/common';
import { log } from '@syndesis/ui/logging';

import { TourService } from 'ngx-tour-ngx-bootstrap';

@Component({
  selector: 'syndesis-integrations-action-configure',
  templateUrl: 'action-configure.component.html',
  styleUrls: ['./action-configure.component.scss']
})
export class IntegrationsConfigureActionComponent extends FlowPage
  implements OnInit, OnDestroy {
  routeSubscription: Subscription;
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

  constructor(
    public currentFlow: CurrentFlow,
    public route: ActivatedRoute,
    public router: Router,
    public formFactory: FormFactoryService,
    public formService: DynamicFormService,
    public integrationSupport: IntegrationSupportService,
    public tourService: TourService,
    private userService: UserService
  ) {
    super(currentFlow, route, router);
  }

  goBack() {
    const step = this.currentFlow.getStep(this.position);
    step.action = undefined;
    super.goBack([ 'action-select', this.position ]);
  }

  buildData(data: any) {
    const formValue = this.formGroup ? this.formGroup.value : {};
    return { ...this.step.configuredProperties, ...formValue, ...data };
  }

  previous(data: any = undefined) {
    data = this.buildData(data);
    this.currentFlow.events.emit({
      kind: 'integration-set-properties',
      position: this.position,
      properties: data,
      onSave: () => {
        if (this.page === 0) {
          /* All done... */
          this.router.navigate([ 'save-or-add-step' ], {
            queryParams: { validate: true },
            relativeTo: this.route.parent
          });
        } else {
          /* Go to the next page... */
          this.router.navigate(
            ['action-configure', this.position, this.page - 1],
            { relativeTo: this.route.parent }
          );
        }
      }
    });
  }

  maybeDisableDone() {
    return this.formGroup ? this.formGroup.invalid : false;
  }

  finishUp() {
    this.router.navigate(['save-or-add-step'], {
      queryParams: { validate: true },
      relativeTo: this.route.parent
    });
  }

  continue(data: any = undefined) {
    this.error = undefined;
    data = this.buildData(data);
    this.currentFlow.events.emit({
      kind: 'integration-set-properties',
      position: this.position,
      properties: data,
      onSave: () => {
        if (!this.lastPage || this.page >= this.lastPage) {
          /**
           * If there are action properties that depend on having other action
           * properties defined in previous steps we need to fetch metadata one
           * more time and apply those action property values that we get
           */
          if (Object.keys(this.step.configuredProperties).length > 0) {
            this.integrationSupport
              .fetchMetadata(this.step.connection, this.step.action, data)
              .toPromise()
              .then(response => {
                log.debug(
                  'Response: ' + JSON.stringify(response, undefined, 2)
                );
                const descriptor: any = response['_body']
                  ? JSON.parse(response['_body'])
                  : undefined;
                for (const actionProperty of Object.keys(data)) {
                  if (data[actionProperty] == null) {
                    this.step.configuredProperties[
                      actionProperty
                    ] = descriptor.propertyDefinitionSteps.map(
                      actionDefinitionStep => {
                        return actionDefinitionStep.properties[actionProperty]
                          .defaultValue;
                      }
                    )[0];
                  }
                }
                /* All done... */
                this.finishUp();
              })
              .catch(response => {
                const body = JSON.parse(response._body);
                this.error = {
                  class: 'alert alert-warning',
                  message:
                    body.message ||
                    body.userMsg ||
                    body.developerMsg
                };
              });
          } else {
            /* All done... */
            this.finishUp();
          }
        } else {
          /* Go to the next wizard page... */
          this.router.navigate(
            ['action-configure', this.position, this.page + 1],
            { relativeTo: this.route.parent }
          );
        }
      }
    });
  }

  initialize(position: number, page: number) {
    this.error = undefined;
    const step = <Step>this.currentFlow.getStep(this.position);
    if (!step) {
      this.router.navigate(['connection-select', this.position], {
        relativeTo: this.route.parent
      });
      return;
    }
    this.action = step.action;
    this.step = step;

    this.integrationSupport
      .fetchMetadata(
        this.step.connection,
        this.step.action,
        this.configuredPropertiesForMetadataCall()
      )
      .toPromise()
      .then(response => {
        log.debug('Response: ' + JSON.stringify(response, undefined, 2));
        const descriptor: any = response[ '_body' ]
          ? JSON.parse(response[ '_body' ])
          : undefined;
        this.initForm(position, page, descriptor);
      })
      .catch(response => {
        log.debug('Error response: ' + JSON.stringify(response, undefined, 2));
        try {
          const error = JSON.parse(response[ '_body' ]);
          this.initForm(position, page, undefined, error);
        } catch (err) {
          /* Bailout at this point... */
          this.initForm(position, page, undefined, {
            message: response['_body']
          });
        }
      });
  }

  initForm(position: number, page: number, descriptor: any, error?: any) {
    if (error) {
      this.error = error;
      this.error.message = error.message || error.userMsg || error.developerMsg;
      this.error.class = 'alert alert-warning';
      this.loading = false;
      return;
    }
    if (!descriptor || descriptor === undefined) {
      this.loading = false;
      // TODO figure out how to get a link in here that works
      this.error = {
        class: 'alert alert-info',
        message: 'There are no properties to configure for this action'
      };
      setTimeout(() => {
        this.continue({});
      }, 1500);
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
      /* No configuration, store an empty value and move along to the next page... */
      this.continue({});
      return;
    }
    this.formModel = this.formFactory.createFormModel(
      this.formConfig,
      this.step.configuredProperties
    );
    this.formGroup = this.formService.createFormGroup(this.formModel);
    setTimeout(() => {
      this.currentFlow.events.emit({
        kind: 'integration-action-configure',
        position: this.position
      });
      this.loading = false;
    }, 30);
  }

  configuredPropertiesForMetadataCall() {
    /**
     * Fetches all properties from 0..this.position-1 steps, this way
     * the backend does not fix a value and drills down into the detail
     * of the selected value in this.position step, this allows enum
     * values to be present in the same way as they are present when no
     * value is selected by the user
     */
    const props = {};
    const stepDefinitions = this.step.action.descriptor.propertyDefinitionSteps;
    for (let p = 0; p < this.page; p++) {
      for (const prop in stepDefinitions[ p ].properties) {
        /* We don't want null or undefined values here */
        if (this.step.configuredProperties[ prop ] != null) {
          props[ prop ] = this.step.configuredProperties[ prop ];
        }
      }
    }

    return props;
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
          params.get('position')
        ));
        this.loading = true;
        this.initialize(position, page);
      }
    );

    /**
     * If guided tour state is set to be shown (i.e. true), then show it for this page, otherwise don't.
     */
    /*
    if (this.userService.getTourState() === true) {
      this.tourService.initialize([ {
          anchorId: 'integrations.done',
          title: 'Done',
          content: 'Clicking Done adds the finish connection to the integration. ' +
          'You can then add one or more steps that operate on the data.',
          placement: 'left',
        } ],
      );
      setTimeout(() => this.tourService.start());
    }
    */
  }

  ngOnDestroy() {
    super.ngOnDestroy();
    this.routeSubscription.unsubscribe();
  }
}
