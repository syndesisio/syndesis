import {
  Component,
  EventEmitter,
  Input,
  Output,
  ViewEncapsulation,
  OnChanges,
  OnDestroy
} from '@angular/core';
import {
  FormGroup,
  FormArray,
  FormBuilder,
  Validators
} from '@angular/forms';

import { of, Observable, Subscription } from 'rxjs';
import { DataShape, IntegrationSupportService } from '@syndesis/ui/platform';
import { log } from '@syndesis/ui/logging';
import { CurrentFlowService } from '@syndesis/ui/integration/edit-page';
import {
  BasicFilter,
  getDefaultOps,
  convertOps,
  Op,
  Rule
} from '@syndesis/ui/integration/edit-page/step-configure/filter-steps/filter.interface';

@Component({
  selector: 'syndesis-basic-filter',
  templateUrl: './basic-filter.component.html',
  encapsulation: ViewEncapsulation.None,
  styleUrls: ['./basic-filter.component.scss']
})
export class BasicFilterComponent implements OnChanges, OnDestroy {
  form: FormGroup;
  rulesArray: any = [];
  ruleOpsModel$: Observable<any>;
  predicateOpsModel$: Observable<any> = of([
    {
      label: 'ALL of the following',
      value: 'AND'
    },
    {
      label: 'ANY of the following',
      value: 'OR'
    }
  ]);
  paths: Array<string>;
  loading = true;
  formValueChangeSubscription: Subscription;

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
    private fb: FormBuilder
  ) {}

  // this can be valid even if we can't fetch the form data
  initForm(
    configuredProperties?: BasicFilter,
    ops: Array<Op> = [],
    paths: Array<string> = []
  ): void {

    if (!ops || !ops.length) {
      ops = getDefaultOps();
    } else {
      ops = convertOps(ops);
    }

    let rules: Rule[] = undefined;
    const incomingGroups = [];
    const incomingPredicate = (configuredProperties && configuredProperties.predicate)
      ? configuredProperties.predicate
      : null;

    // build up the form array from the incoming values (if any)
    if (configuredProperties && configuredProperties.rules) {
      // TODO hackity hack
      if (typeof configuredProperties.rules === 'string') {
        rules = JSON.parse(<any>configuredProperties.rules);
      } else {
        rules = configuredProperties.rules;
      }

      for (const incomingRule of rules) {
        incomingGroups.push(this.fb.group(incomingRule));
      }
    }

    this.ruleOpsModel$ = of(ops);
    this.paths = paths;

    const preloadedPredicate = this.createFilterSettingsGroup(incomingPredicate);

    let preloadedRulesArray;

    if (incomingGroups.length > 0) {
      preloadedRulesArray = this.fb.array(incomingGroups);
    } else {
      preloadedRulesArray = this.fb.array([this.createNewRuleGroup()]);
    }

    this.rulesArray = preloadedRulesArray;

    const formGroupObj = {
      filterSettingsGroup: preloadedPredicate,
      rulesArray: preloadedRulesArray
    };

    this.form = this.fb.group(formGroupObj);

    this.formValueChangeSubscription = this.form.valueChanges.subscribe(_ => {
      this.valid = this.form.valid;
      this.validChange.emit(this.valid);
    });

    this.loading = false;
  }

  createFilterSettingsGroup(predicate?: string): FormGroup {
    return this.fb.group({
      predicate: [predicate || 'AND', null],
      predicateLabel: 'Continue only if incoming data match'
    });
  }

  ngOnDestroy() {
    this.formValueChangeSubscription.unsubscribe();
  }

  ngOnChanges(changes: any) {
    if (!('position' in changes)) {
      return;
    }

    this.loading = true;
    // Fetch our form data
    this.integrationSupportService
      .getFilterOptions(this.dataShape)
      .toPromise()
      .then((body: any) => {
        const ops = body.ops;
        const paths = body.paths;
        this.ruleOpsModel$ = of(ops);
        this.paths = paths;
        this.initForm(this.configuredProperties || <any>{}, ops, paths);
      })
      .catch(error => {
        try {
          log.info(
            () => 'Failed to fetch filter form data: ' + JSON.parse(error)
          );
        } catch (err) {
          log.info(() => 'Failed to fetch filter form data: ' + error);
        }
        // we can handle this for now using default values
        this.initForm();
      });
  }

  addRuleSet(): void {
    const newGroup = <FormGroup>this.createNewRuleGroup();
    this.rulesArray = this.form.get('rulesArray') as FormArray;
    this.rulesArray.push(newGroup);
  }

  get myRules(): FormArray {
    return <FormArray>this.rulesArray;
  }

  removeRuleSet(index: number): void {
    this.myRules.removeAt(index);
    this.onChange();
  }

  createNewRuleGroup(ops?, paths?, value?): FormGroup {
    const group = {
      path: ['', Validators.compose([
          Validators.required,
          Validators.maxLength(51)
        ])
      ],
      op: ['contains', Validators.required],
      value: ['', Validators.required]
    };
    return this.fb.group(group);
  }

  onChange() {
    this.valid = this.form.valid;
    this.validChange.emit(this.valid);
    if (!this.valid) {
      return;
    }

    const formGroupObj = this.form.value;

    const formattedProperties: BasicFilter = {
      type: 'rule',
      predicate: this.form.controls.filterSettingsGroup.get('predicate').value,
      rules: formGroupObj.rulesArray
    };

    this.configuredPropertiesChange.emit(formattedProperties);
  }
}
