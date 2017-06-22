import { Component, EventEmitter, Input, OnInit, Output, ViewEncapsulation } from '@angular/core';
import { FormGroup, FormControl, FormArray } from '@angular/forms';
import {
  DynamicFormService,
  DynamicFormControlModel,
  DynamicFormArrayModel,
  DynamicInputModel,
} from '@ng2-dynamic-forms/core';

import { BASIC_FILTER_MODEL } from './basic-filter.model';
import { log, getCategory } from '../../../../logging';
import { BasicFilter } from './filter.interface';

@Component({
  selector: 'syndesis-basic-filter',
  templateUrl: './basic-filter.component.html',
  encapsulation: ViewEncapsulation.None,
})

export class BasicFilterComponent implements OnInit {

  basicFilterModel: DynamicFormControlModel[] = BASIC_FILTER_MODEL;
  formGroup: FormGroup;

  exampleControl: FormControl;
  exampleModel: DynamicInputModel;

  arrayControl: FormArray;
  arrayModel: DynamicFormArrayModel;

  @Input()
  basicFilterObject: BasicFilter = {
    'id': '1',
    'stepKind': 'filter',
    'configuredProperties': {
      'type': 'rule',
      'predicate': 'AND',
      'simple' : '${body} contains \'antman\' || ${in.header.publisher} =~ \'DC Comics\'',
      'rules' : [
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
    },
  };
  @Output() filterChange = new EventEmitter<BasicFilter>();

  constructor(private formService: DynamicFormService) {
  }

  ngOnInit() {
    this.formGroup = this.formService.createFormGroup(this.basicFilterModel);

    this.exampleControl = this.formGroup.get('filterSettingsGroup').get('matchSelect') as FormControl;
    this.exampleModel = this.formService.findById('matchSelect', this.basicFilterModel) as DynamicInputModel;

    this.arrayControl = this.formGroup.get('rulesGroup').get('bootstrapFormArray') as FormArray;
    this.arrayModel = this.formService.findById('rulesFormArray', this.basicFilterModel) as DynamicFormArrayModel;
  }

  // Manage Individual Fields
  add() {
    this.formService.addFormArrayGroup(this.arrayControl, this.arrayModel);
  }

  insert(context: DynamicFormArrayModel, index: number) {
    this.formService.insertFormArrayGroup(index, this.arrayControl, context);
    log.debugc(
      () => 'basicFilterModel: ' + this.basicFilterModel,
    );
  }

  remove(context: DynamicFormArrayModel, index: number) {
    this.formService.removeFormArrayGroup(index, this.arrayControl, context);
  }

  clear() {
    this.formService.clearFormArray(this.arrayControl, this.arrayModel);
  }

  onBlur($event) {
    log.debugc(
      () => 'BLUR event on $(event.model.id): ' + $event,
    );
  }

  onChange($event) {
    this.basicFilterObject = $event.value;
    this.filterChange.emit(this.basicFilterObject);
    log.debugc(
      () => 'CHANGE event on $(event.model.id): ' + $event,
    );
  }

  onFocus($event) {
    log.debugc(
      () => 'FOCUS event on $(event.model.id): ' + $event,
    );
  }
}
