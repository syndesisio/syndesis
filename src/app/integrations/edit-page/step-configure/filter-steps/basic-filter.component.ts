import {
  Component,
  EventEmitter,
  Input,
  Output,
  ViewEncapsulation,
  OnChanges,
  ChangeDetectorRef,
} from '@angular/core';
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
  styleUrls: ['./basic-filter.component.scss'],
})
export class BasicFilterComponent implements OnChanges {
  basicFilterModel: DynamicFormControlModel[];
  formGroup: FormGroup;
  predicateControl: FormControl;
  predicateModel: DynamicInputModel;
  rulesArrayControl: FormArray;
  rulesArrayModel: DynamicFormArrayModel;
  loading = true;

  @Input() position;
  @Input()
  configuredProperties: BasicFilter = {
    type: 'rule',
    predicate: 'AND',
    simple:
      "${body} contains 'antman' || ${in.header.publisher} =~ 'DC Comics'",
    rules: [
      {
        path: 'body.text',
        value: 'antman',
      },
      {
        path: 'header.kind',
        op: '=~',
        value: 'DC Comics',
      },
    ],
  };
  @Output() configuredPropertiesChange = new EventEmitter<BasicFilter>();

  constructor(
    public currentFlow: CurrentFlow,
    public integrationSupport: IntegrationSupportService,
    private formService: DynamicFormService,
    private detector: ChangeDetectorRef,
  ) {}

  ngOnChanges(changes: any) {
    if (!('position' in changes)) {
      return;
    }
    this.loading = true;
    const self = this;

    // this can be valid even if we can't fetch the form data
    function initializeForm(ops?, paths?) {
      self.basicFilterModel = createBasicFilterModel(
        self.configuredProperties,
        ops,
        paths,
      );
      self.formGroup = self.formService.createFormGroup(self.basicFilterModel);
      self.predicateControl = self.formGroup
        .get('filterSettingsGroup')
        .get('predicate') as FormControl;
      self.predicateModel = findById(
        'predicate',
        self.basicFilterModel,
      ) as DynamicInputModel;
      self.rulesArrayControl = self.formGroup
        .get('rulesGroup')
        .get('rulesFormArray') as FormArray;
      self.rulesArrayModel = findById(
        'rulesFormArray',
        self.basicFilterModel,
      ) as DynamicFormArrayModel;
      self.loading = false;
      self.detector.detectChanges();
    }

    // Fetch our form data
    this.integrationSupport
      .getFilterOptions(this.currentFlow.getIntegrationClone())
      .toPromise()
      .then((resp: any) => {
        const body = JSON.parse(resp['_body']);
        const ops = body.ops;
        const paths = body.paths;
        initializeForm(ops, paths);
      })
      .catch(error => {
        try {
          log.infoc(
            () => 'Failed to fetch filter form data: ' + JSON.parse(error),
          );
        } catch (err) {
          log.infoc(() => 'Failed to fetch filter form data: ' + error);
        }
        // we can handle this for now using default values
        initializeForm();
      });
  }

  // Manage Individual Fields
  add() {
    this.formService.addFormArrayGroup(
      this.rulesArrayControl,
      this.rulesArrayModel,
    );
  }

  remove(context: DynamicFormArrayModel, index: number) {
    this.formService.removeFormArrayGroup(
      index,
      this.rulesArrayControl,
      context,
    );
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
