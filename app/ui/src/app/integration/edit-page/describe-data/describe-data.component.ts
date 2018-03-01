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
    displayName: 'Select Type',
    defaultValue: DataShapeKinds.ANY,
    enum: [
      {
        label: 'Don\'t specify type',
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
      }
    ]
  },
  specification: {
    type: 'textarea',
    displayName: 'Definition',
    rows: 10
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

  buttonText = 'Next';
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
  userDefined: boolean;

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

  showDone() {
    if (this.position === 0) {
      return true;
    }
    if (this.currentFlowService.atEnd(this.position)) {
      return true;
    }
    return this.inputSet || this.outputSet;
  }

  validateDataShapes() {
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

  continue() {
    const step = this.currentFlowService.getStep(this.position);
    if (this.userDefined) {
      const value = this.formGroup.value;
      const dataShape = this.getDataShape(this.direction, step);
      // normalize this to 'any'
      dataShape.kind = value.kind;
      dataShape.specification = value.specification || '';
      dataShape.name = value.name || '';
      dataShape.description = value.description || '';
      dataShape.metadata = { ...dataShape.metadata, ...{ userDefined: 'true' }};
      this.currentFlowService.events.emit({
        kind: 'integration-set-datashape',
        isInput: this.direction === DataShapeDirection.INPUT,
        dataShape,
        onSave: () => {
          this.validateDataShapes();
        }
      });
    } else {
      this.validateDataShapes();
    }
  }

  initialize() {
    if (!this.currentFlowService.loaded || this.position === undefined) {
      return;
    }
    this.userDefined = false;
    this.formValues = undefined;
    this.formModel = undefined;
    const step = this.currentFlowService.getStep(this.position);
    if (!step.action) {
      this.router.navigate(['action-select', this.position], {
        relativeTo: this.route.parent
      });
      return;
    }
    this.connectionName = step.connection.name;
    this.actionName = step.action.name;
    if (this.showDone()) {
      this.buttonText = 'Done';
    }
    const dataShape = this.getDataShape(this.direction, step);
    if (dataShape.kind === DataShapeKinds.ANY || (dataShape.metadata && dataShape.metadata.userDefined === 'true')) {
      this.formValues = {
        kind: dataShape.kind,
        name: dataShape.name,
        description: dataShape.description,
        specification: dataShape.specification
      } as StringMap<string>;
      this.userDefined = true;
      this.formModel = this.formFactoryService.createFormModel(DESCRIBE_DATA_FORM_CONFIG, this.formValues);
      this.formGroup = this.dynamicFormService.createFormGroup(this.formModel);
    } else {
      // no need to prompt...
      this.userDefined = false;
      this.continue();
    }
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
