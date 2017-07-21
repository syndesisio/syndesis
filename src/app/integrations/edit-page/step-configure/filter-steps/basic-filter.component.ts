import { Component, EventEmitter, Input, OnInit, Output, ViewEncapsulation } from '@angular/core';
import { FormGroup, FormControl, FormArray } from '@angular/forms';
import {
  DynamicFormService,
  DynamicFormControlModel,
  DynamicFormArrayModel,
  DynamicInputModel,
} from '@ng2-dynamic-forms/core';

import { CurrentFlow, FlowEvent } from '../../current-flow.service';

import { BASIC_FILTER_MODEL } from './basic-filter.model';
import { log, getCategory } from '../../../../logging';
import { BasicFilter } from './filter.interface';

@Component({
  selector: 'syndesis-basic-filter',
  templateUrl: './basic-filter.component.html',
  encapsulation: ViewEncapsulation.None,
  styleUrls: [ './basic-filter.component.scss' ],
})

export class BasicFilterComponent implements OnInit {

  basicFilterModel: DynamicFormControlModel[] = BASIC_FILTER_MODEL;
  formGroup: FormGroup;

  predicateControl: FormControl;
  predicateModel: DynamicInputModel;

  rulesArrayControl: FormArray;
  rulesArrayModel: DynamicFormArrayModel;

  @Input()
  basicFilterObject: BasicFilter = {
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
  @Output() filterChange = new EventEmitter<BasicFilter>();

  constructor(public currentFlow: CurrentFlow,
              private formService: DynamicFormService) {
  }

  ngOnInit() {
    this.currentFlow.getFilterOptions().toPromise().then((resp: any) => {
      log.info('Filter option response: ' + JSON.stringify(resp));
    });

    log.info('this.basicFilterObject: ' + JSON.stringify(this.basicFilterObject));

    this.formGroup = this.formService.createFormGroup(this.basicFilterModel);

    this.predicateControl = this.formGroup.get('filterSettingsGroup').get('predicate') as FormControl;
    this.predicateModel = this.formService.findById('predicate', this.basicFilterModel) as DynamicInputModel;

    this.rulesArrayControl = this.formGroup.get('rulesGroup').get('rulesFormArray') as FormArray;
    this.rulesArrayModel = this.formService.findById('rulesFormArray', this.basicFilterModel) as DynamicFormArrayModel;
  }

  // Manage Individual Fields
  add() {
    this.formService.addFormArrayGroup(this.rulesArrayControl, this.rulesArrayModel);
    //log.info('basicFilterModel: ' + JSON.stringify(this.basicFilterModel));
  }

  remove(context: DynamicFormArrayModel, index: number) {
    this.formService.removeFormArrayGroup(index, this.rulesArrayControl, context);
  }

  onChange($event) {
    const formGroupObj = this.formGroup.value;
    //log.info('rulesGroup: ' + JSON.stringify(formGroupObj));

    const formattedProperties: BasicFilter = {
      type: 'rule',
      predicate: formGroupObj.filterSettingsGroup.predicate,
      rules: formGroupObj.rulesGroup.rulesFormArray,
    };

    //log.info('this.formGroup.value: ' + JSON.stringify(this.formGroup.value));

    this.filterChange.emit(formattedProperties);

    log.info('formattedProperties: ' + JSON.stringify(formattedProperties));
  }
}
