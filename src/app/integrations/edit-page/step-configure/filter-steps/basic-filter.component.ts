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
    'id': '1',
    'stepKind': 'filter',
    'configuredProperties': {
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
    },
  };
  @Output() filterChange = new EventEmitter<BasicFilter>();

  constructor(public currentFlow: CurrentFlow,
              private formService: DynamicFormService) {
  }

  ngOnInit() {
    this.currentFlow.getFilterOptions().toPromise().then((resp: any) => {
      log.info('Filter option response: ' + JSON.stringify(resp));
    });

    this.formGroup = this.formService.createFormGroup(this.basicFilterModel);

    this.predicateControl = this.formGroup.get('filterSettingsGroup').get('predicate') as FormControl;
    this.predicateModel = this.formService.findById('predicate', this.basicFilterModel) as DynamicInputModel;

    this.rulesArrayControl = this.formGroup.get('rulesGroup').get('rulesFormArray') as FormArray;
    this.rulesArrayModel = this.formService.findById('rulesFormArray', this.basicFilterModel) as DynamicFormArrayModel;
  }

  // Manage Individual Fields
  add() {
    this.formService.addFormArrayGroup(this.rulesArrayControl, this.rulesArrayModel);
    log.info('basicFilterModel: ' + JSON.stringify(this.basicFilterModel));
  }

  remove(context: DynamicFormArrayModel, index: number) {
    this.formService.removeFormArrayGroup(index, this.rulesArrayControl, context);
  }

  onChange($event) {
    //this.formService.findById('path', this.rulesArrayModel).value;
    const formattedRules = [];
    const formattedRule = {
      path: '',
      op: '',
      value: '',
    };

    //const json: string = JSON.stringify(this.basicFilterModel);
    //log.info('json Form: ' + json);
    //log.info('predicateModel: ' + JSON.stringify(this.predicateModel));
    //log.info('rulesArrayModel: ' + JSON.stringify(this.rulesArrayModel));
    //log.info('rulesArrayModel.groups: ' + JSON.stringify(this.rulesArrayModel.groups));
    log.info('rulesArrayModel.groupPrototype: ' + JSON.stringify(this.rulesArrayModel.groupPrototype));

    for (const rule of this.rulesArrayModel.groupPrototype) {
      log.info('Rule: ' + JSON.stringify(rule));
      /*
      log.info('rule.path: ' + JSON.stringify(rule.id === 'path' ? rule['value'] : null));
      log.info('rule.op: ' + JSON.stringify(rule.id === 'op' ? rule['value'] : null));
      log.info('rule.value: ' + JSON.stringify(rule.id === 'value' ? rule['value'] : null));
      */

      switch(rule.id) {
        case 'path':
          formattedRule.path = rule['value'];
          break;
        case 'op':
          formattedRule.op = rule['value'];
          break;
        case 'value':
          formattedRule.value = rule['value'];
          break;
      }
      log.info('Pushing formattedRule to formattedRules array: ' + JSON.stringify(formattedRule));
      formattedRules.push(formattedRule);
/*
      formattedRules.push({
        path: rule.id === 'path' ? rule['value'] : null,
        op: rule.id === 'op' ? rule['value'] : null,
        value: rule.id === 'value' ? rule['value'] : null,
      });
      */
    }
    log.info('findById path: ' + JSON.stringify(this.formService.findById('path', this.rulesArrayModel.groupPrototype)));

    /*
    this.basicFilterObject.configuredProperties = {
      type: 'rule',
      predicate: this.predicateModel.value,
      rules: [
        {
          path: '',
          op: '',
          value: '',
        },
      ],
    };
    */

    log.info('this.basicFilterObject.configuredProperties: ' + JSON.stringify(this.basicFilterObject.configuredProperties));


    this.filterChange.emit(this.basicFilterObject);
    log.info('this.basicFilterObject: ' + JSON.stringify(this.basicFilterObject));
    //log.info('this.basicFilterModel: ' + JSON.stringify(this.basicFilterModel));
    //log.info('CHANGE event on $(event.model.id): ' + $event);
  }
}
