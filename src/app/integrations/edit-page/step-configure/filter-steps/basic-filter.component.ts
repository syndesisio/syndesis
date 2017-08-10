import { Component, EventEmitter, Input, Output, ViewEncapsulation, OnChanges } from '@angular/core';
import { FormGroup, FormControl, FormArray } from '@angular/forms';
import {
  DynamicFormService,
  DynamicFormControlModel,
  DynamicFormArrayModel,
  DynamicInputModel,
} from '@ng2-dynamic-forms/core';

import { CurrentFlow, FlowEvent } from '../../current-flow.service';
import { IntegrationSupportService } from '../../../../store/integration-support.service';

import { createBasicFilterModel, findById } from './basic-filter.model';
import { log, getCategory } from '../../../../logging';
import { BasicFilter } from './filter.interface';

@Component({
  selector: 'syndesis-basic-filter',
  templateUrl: './basic-filter.component.html',
  encapsulation: ViewEncapsulation.None,
  styleUrls: [ './basic-filter.component.scss' ],
})

export class BasicFilterComponent implements OnChanges {

  basicFilterModel: DynamicFormControlModel[];
  formGroup: FormGroup;
  predicateControl: FormControl;
  predicateModel: DynamicInputModel;
  rulesArrayControl: FormArray;
  rulesArrayModel: DynamicFormArrayModel;

  @Input() position;
  @Input()
  configuredProperties: BasicFilter = {
    'type': 'rule',
    'predicate': 'AND',
    'simple': '${body} contains \'antman\' || ${in.header.publisher} =~ \'DC Comics\'',
    'rules': [
      {
        'path': 'body.text',
        'value': 'antman',
      },
      {
        'path': 'header.kind',
        'op': '=~',
        'value': 'DC Comics',
      },
    ],
  };
  @Output() configuredPropertiesChange = new EventEmitter<BasicFilter>();

  constructor(public currentFlow: CurrentFlow,
              public integrationSupport: IntegrationSupportService,
              private formService: DynamicFormService) {
  }

  ngOnChanges() {
    this.basicFilterModel = createBasicFilterModel(this.configuredProperties);

    this.integrationSupport.getFilterOptions(this.currentFlow.getIntegrationClone()).toPromise().then((resp: any) => {
      console.dir(JSON.parse(resp['_body']));
      log.info('Filter option response: ' + JSON.stringify(resp));
    });

    this.formGroup = this.formService.createFormGroup(this.basicFilterModel);

    this.predicateControl = this.formGroup.get('filterSettingsGroup').get('predicate') as FormControl;
    this.predicateModel = findById('predicate', this.basicFilterModel) as DynamicInputModel;

    this.rulesArrayControl = this.formGroup.get('rulesGroup').get('rulesFormArray') as FormArray;
    this.rulesArrayModel = findById('rulesFormArray', this.basicFilterModel) as DynamicFormArrayModel;
  }

  // Manage Individual Fields
  add() {
    this.formService.addFormArrayGroup(this.rulesArrayControl, this.rulesArrayModel);
  }

  remove(context: DynamicFormArrayModel, index: number) {
    this.formService.removeFormArrayGroup(index, this.rulesArrayControl, context);
  }

  onChange($event) {
    const formGroupObj = this.formGroup.value;

    const formattedProperties: BasicFilter = {
      type: 'rule',
      predicate: formGroupObj.filterSettingsGroup.predicate,
      rules: formGroupObj.rulesGroup.rulesFormArray,
    };

    this.configuredPropertiesChange.emit(formattedProperties);
  }
}
