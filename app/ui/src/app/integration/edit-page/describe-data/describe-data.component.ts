import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { CurrentFlowService, FlowPageService, FlowEvent } from '../index';
import { ActivatedRoute, Router, ParamMap } from '@angular/router';
import { Step, FormFactoryService, StringMap, DataShapeKinds } from '@syndesis/ui/platform';
import { Observable } from 'rxjs/Observable';
import { DynamicFormService, DynamicFormControlModel } from '@ng-dynamic-forms/core';
import { FormGroup } from '@angular/forms';

enum DataShapeDirection {
  INPUT = 'input',
  OUTPUT = 'output'
}
const DESCRIBE_DATA_FORM_CONFIG = {
  kind: {
    type: 'select',
    displayName: 'Select Schema Type',
    defaultValue: 'json-schema',
    enum: [
      {
        label: 'No Type',
        value: DataShapeKinds.NONE
      },
      {
        label: 'Any Type',
        value: DataShapeKinds.ANY
      },
      {
        label: 'JSON Schema',
        value: DataShapeKinds.JSON_SCHEMA
      },
      {
        label: 'JSON Instance',
        value: DataShapeKinds.JSON_INSTANCE
      },
      {
        label: 'XML Schema',
        value: DataShapeKinds.XML_SCHEMA
      },
      {
        label: 'XML Instance',
        value: DataShapeKinds.XML_INSTANCE
      },
      {
        label: 'Java',
        value: DataShapeKinds.JAVA
      }
    ]
  },
  specification: {
    type: 'textarea',
    displayName: 'Schema'
  },
  name: {
    type: 'string',
    displayName: 'Data Type Name'
  },
  description: {
    type: 'string',
    displayName: 'Data Type Description'
  }
};

@Component({
  selector: 'syndesis-integration-describe-data',
  templateUrl: 'describe-data.component.html'
})
export class IntegrationDescribeDataComponent implements OnInit, OnDestroy {

  actionName: string;
  connectionName: string;
  formGroup: FormGroup;
  formModel: DynamicFormControlModel[];
  flowSubscription: Subscription;
  routeSubscription: Subscription;
  position: number;
  direction: DataShapeDirection;
  formValues: StringMap<string>;
  inputSet: boolean;
  outputSet: boolean;

  constructor(
    public currentFlowService: CurrentFlowService,
    public flowPageService: FlowPageService,
    public route: ActivatedRoute,
    public router: Router,
    public formFactoryService: FormFactoryService,
    public dynamicFormService: DynamicFormService
  ) {
    // nothing to do
  }

  finishUp() {
    this.router.navigate(['save-or-add-step'], {
      queryParams: { validate: true },
      relativeTo: this.route.parent
    });
  }

  continue() {
    const step = this.currentFlowService.getStep(this.position);
    if (this.formGroup) {
      const value = this.formGroup.value;
      const dataShape = this.getDataShape(this.direction, step);
      // normalize this to 'any'
      dataShape.kind = value.kind;
      dataShape.specification = value.specification || '';
      dataShape.name = value.name || '';
      dataShape.description = value.description || '';
    }
    if (this.direction === DataShapeDirection.INPUT) {
      this.inputSet = true;
    }
    if (this.direction === DataShapeDirection.OUTPUT) {
      this.outputSet = true;
    }
    // if we're at the start we only need an output
    if (this.position === 0 && this.outputSet) {
      this.finishUp();
      return;
    }
    // if we're at the end we only need an input
    if (this.currentFlowService.atEnd(this.position) && this.inputSet) {
      this.finishUp();
      return;
    }
    // both have been set
    if (this.inputSet && this.outputSet) {
      this.finishUp();
      return;
    }
    // if we're in the middle, we need to check both
    const nextDirection = this.direction === DataShapeDirection.INPUT ? DataShapeDirection.OUTPUT : DataShapeDirection.INPUT;
    this.router.navigate(['describe-data', this.position, nextDirection], { relativeTo: this.route.parent });
  }

  initialize() {
    if (!this.currentFlowService.loaded || this.position === undefined) {
      return;
    }
    const step = this.currentFlowService.getStep(this.position);
    if (!step.action) {
      this.router.navigate(['action-select', this.position], {
        relativeTo: this.route.parent
      });
      return;
    }
    this.connectionName = step.connection.name;
    this.actionName = step.action.name;
    const dataShape = this.getDataShape(this.direction, step);
    /*
    if (dataShape.kind === DataShapeKinds.ANY) {
    */
    this.formValues = {
        kind: dataShape.kind,
        name: dataShape.name,
        description: dataShape.description,
        specification: dataShape.specification
      } as StringMap<string>;
    this.formModel = this.formFactoryService.createFormModel(DESCRIBE_DATA_FORM_CONFIG, this.formValues);
    this.formGroup = this.dynamicFormService.createFormGroup(this.formModel);
    /*
    } else {
      // no need to prompt...
      this.continue();
    }
    */
  }

  ngOnInit() {
    this.routeSubscription = this.route.paramMap.subscribe((params: ParamMap) => {
      this.direction = params.get('direction') as DataShapeDirection;
      this.position = +params.get('position');
      this.initialize();
    });
    this.flowSubscription = this.currentFlowService.events.subscribe((flowEvent: FlowEvent) => {
      if (flowEvent.kind === 'integration-updated') {
        this.initialize();
      }
    });
  }

  ngOnDestroy() {
    this.flowSubscription.unsubscribe();
    this.routeSubscription.unsubscribe();
  }

  private getDataShape(direction: DataShapeDirection, step: Step) {
    if (direction === DataShapeDirection.INPUT) {
      return step.action.descriptor.inputDataShape;
    }
    return step.action.descriptor.outputDataShape;
  }

}
