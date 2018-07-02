import {
  Component,
  EventEmitter,
  Input,
  Output,
  ViewEncapsulation,
  OnChanges
} from '@angular/core';
import { FormGroup, FormControl, FormArray } from '@angular/forms';
import {
  DynamicFormService,
  DynamicFormControlModel,
  DynamicFormArrayModel,
  DynamicInputModel
} from '@ng-dynamic-forms/core';

import { DataShape, IntegrationSupportService } from '@syndesis/ui/platform';
import { log, getCategory } from '@syndesis/ui/logging';
import { DATA_MAPPER } from '@syndesis/ui/store';
import {
  CurrentFlowService,
  FlowEvent
} from '@syndesis/ui/integration/edit-page';
import { createBasicFilterModel, findById } from './basic-filter.model';
import { BasicFilter } from './filter.interface';

@Component({
  selector: 'syndesis-basic-filter',
  templateUrl: './basic-filter.component.html',
  encapsulation: ViewEncapsulation.None,
  styleUrls: ['./basic-filter.component.scss']
})
export class BasicFilterComponent implements OnChanges {
  basicFilterModel: DynamicFormControlModel[];
  formGroup: FormGroup;
  predicateControl: FormControl;
  predicateModel: DynamicInputModel;
  rulesArrayControl: FormArray;
  rulesArrayModel: DynamicFormArrayModel;
  loading = true;

  @Input() dataShape: DataShape;
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
        value: 'antman'
      },
      {
        path: 'header.kind',
        op: '=~',
        value: 'DC Comics'
      }
    ]
  };
  @Output() configuredPropertiesChange = new EventEmitter<BasicFilter>();
  @Input() valid: boolean;
  @Output() validChange = new EventEmitter<boolean>();

  constructor(
    public currentFlowService: CurrentFlowService,
    public integrationSupportService: IntegrationSupportService,
    private dynamicFormService: DynamicFormService
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
        self.configuredProperties || <any>{},
        ops,
        paths
      );
      self.formGroup = self.dynamicFormService.createFormGroup(
        self.basicFilterModel
      );
      self.predicateControl = self.formGroup
        .get('filterSettingsGroup')
        .get('predicate') as FormControl;
      self.predicateModel = findById(
        'predicate',
        self.basicFilterModel
      ) as DynamicInputModel;
      self.rulesArrayControl = self.formGroup
        .get('rulesGroup')
        .get('rulesFormArray') as FormArray;
      self.rulesArrayModel = findById(
        'rulesFormArray',
        self.basicFilterModel
      ) as DynamicFormArrayModel;
      self.loading = false;
      self.valid = self.formGroup.valid;
      self.validChange.emit(self.formGroup.valid);
    }

    // Fetch our form data
    this.integrationSupportService
      .getFilterOptions(this.dataShape)
      .toPromise()
      .then((body: any) => {
        const ops = body.ops;
        const paths = body.paths;
        initializeForm(ops, paths);
      })
      .catch(error => {
        try {
          log.infoc(
            () => 'Failed to fetch filter form data: ' + JSON.parse(error)
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
    this.dynamicFormService.addFormArrayGroup(
      this.rulesArrayControl,
      this.rulesArrayModel
    );
    this.valid = this.formGroup.valid;
    this.validChange.emit(this.valid);
  }

  remove(context: DynamicFormArrayModel, index: number) {
    this.dynamicFormService.removeFormArrayGroup(
      index,
      this.rulesArrayControl,
      context
    );
    this.valid = this.formGroup.valid;
    this.validChange.emit(this.valid);
  }

  onChange($event) {
    this.valid = this.formGroup.valid;
    this.validChange.emit(this.valid);
    if (!this.valid) {
      return;
    }
    const formGroupObj = this.formGroup.value;

    const formattedProperties: BasicFilter = {
      type: 'rule',
      predicate: formGroupObj.filterSettingsGroup.predicate,
      rules: formGroupObj.rulesGroup.rulesFormArray
    };

    this.configuredPropertiesChange.emit(formattedProperties);
  }
}
