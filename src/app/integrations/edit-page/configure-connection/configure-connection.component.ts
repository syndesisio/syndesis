import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { FormGroup } from '@angular/forms';
import { DynamicFormControlModel, DynamicFormService } from '@ng2-dynamic-forms/core';

import { FormFactoryService } from '../../../common/forms.service';
import { CurrentFlow, FlowEvent } from '../current-flow.service';
import { Connection } from '../../../model';
import { log, getCategory } from '../../../logging';

const category = getCategory('IntegrationsCreatePage');

@Component({
  selector: 'ipaas-integrations-configure-connection',
  templateUrl: 'configure-connection.component.html',
})
export class IntegrationsConfigureConnectionComponent implements OnInit, OnDestroy {

  flowSubscription: Subscription;
  routeSubscription: Subscription;
  position: number;
  connection: Connection = <Connection>{ };
  formModel: DynamicFormControlModel[];
  formGroup: FormGroup;

  constructor(
    private currentFlow: CurrentFlow,
    private route: ActivatedRoute,
    private router: Router,
    private formFactory: FormFactoryService,
    private formService: DynamicFormService,
  ) {

  }

  handleFlowEvent(event: FlowEvent) {
    switch (event.kind) {
      case 'integration-no-connection':
        break;
    }
  }

  ngOnInit() {
    this.flowSubscription = this.currentFlow.events.subscribe((event: FlowEvent) => {
      this.handleFlowEvent(event);
    });
    this.routeSubscription = this.route.params.pluck<Params, string>('position')
      .map((position: string) => {
        this.position = Number.parseInt(position);
        this.connection = <Connection> this.currentFlow.getStep(this.position);
        if (this.connection && this.connection.configuredProperties) {
          const configString = this.connection.configuredProperties;
          // TODO giant hack so we see something on the config page
          let formConfig = undefined;
          try {
            formConfig = JSON.parse(configString);
          } catch (err) {
            log.debugc(() => 'Error parsing form config', category);
          }
          log.debugc(() => 'Form config: ' + JSON.stringify(formConfig, undefined, 2), category);
          this.formModel = this.formFactory.createFormModel(formConfig);
          log.debugc(() => 'Form model: ' + JSON.stringify(this.formModel, undefined, 2), category);
          this.formGroup = this.formService.createFormGroup(this.formModel);
        } else {
          // TODO this case needs to be dealt with
          this.formModel = undefined;
          this.formGroup = undefined;
        }
        this.currentFlow.events.emit({
          kind: 'integration-connection-configure',
          position: this.position,
        });
      })
      .subscribe();
  }

  ngOnDestroy() {
    this.routeSubscription.unsubscribe();
    this.flowSubscription.unsubscribe();
  }
}
