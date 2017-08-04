import {
  DynamicInputModel,
  DynamicSelectModel,
  DynamicFormArrayModel,
  DynamicFormArrayGroupModel,
  DynamicFormGroupModel,
} from '@ng2-dynamic-forms/core';
import { Observable } from 'rxjs/Observable';

import { BasicFilter, Rule } from './filter.interface';

export function findById(id: string, model: any): any {
  switch (typeof model) {
    case 'object':
      if (Array.isArray(model)) {
        const answer = (<Array<any>>model).find(i => findById(id, i));
        if (!answer) {
          return false;
        }
        return answer['group'] ? findById(id, answer['group']) : answer;
      } else {
        if (model['id'] && model['id'] === id) {
          return model;
        }
        if (model['group']) {
          const answer = findById(id, model['group']);
          return answer;
        }
      }
  }
  return false;
}

export function createBasicFilterModel(configuredProperties: BasicFilter) {
  // inline this function since we use it in a couple places
  function groupFactory(rule?) {
    return [
      new DynamicInputModel(
        {
          id: 'path',
          maxLength: 51,
          placeholder: 'Status.Text',
          value: rule ? rule.path : undefined,
          //suffix: 'Browse...', // This is just a suffix; this whole field needs to change
        },
        {
          element: {
            container: 'form-group col-xs-3',
          },
          grid: {
            control: 'input-group',
          },
        },
      ),
      new DynamicSelectModel<string>(
        {
          id: 'op',
          value: rule ? rule.op : 'contains',
          options: Observable.of([
            {
              label: 'Contains',
              value: 'contains',
            },
            {
              label: 'Does Not Contain',
              value: 'not contains',
            },
            {
              label: 'Matches Regex',
              value: 'regex',
            },
            {
              label: 'Does Not Match Regex',
              value: 'not regex',
            },
            {
              label: 'Starts With',
              value: 'starts with',
            },
            {
              label: 'Ends With',
              value: 'ends with',
            },
          ]),
        },
        {
          element: {
            container: 'form-group col-xs-3',
            label: 'control-label',
          },
          grid: {
            control: 'input-group',
          },
        },
      ),
      new DynamicInputModel(
        {
          id: 'value',
          value: rule ? rule.value : undefined,
          placeholder: 'Keywords...',
        },
        {
          grid: {
            container: 'input-group col-xs-4 keywords',
          },
        },
      ),
    ];
  };
  let groups = undefined;
  // build up the form array from the incoming values (if any)
  if (configuredProperties && configuredProperties.rules && configuredProperties.rules.length) {
    groups = [];
    for (const rule of configuredProperties.rules) {
      groups.push(new DynamicFormArrayGroupModel(undefined, groupFactory(rule)));
    }
  }
  const answer = [
    new DynamicFormGroupModel({
      id: 'filterSettingsGroup',
      group: [
        new DynamicSelectModel<string>(
          {
            id: 'predicate',
            label: 'Continue only if incoming data match ',
            options: Observable.of([
              {
                label: 'ALL of the following',
                value: 'AND',
              },
              {
                label: 'ANY of the following',
                value: 'OR',
              },
            ]),
            value: configuredProperties.predicate || 'AND',
          },
          {
            element: {
              label: 'control-label pull-left',
            },
            grid: {
              control: 'col-xs-3 match-select',
            },
          },
        ),
      ],
    }),

    new DynamicFormGroupModel({
      id: 'rulesGroup',
      group: [
        new DynamicFormArrayModel(
          {
            id: 'rulesFormArray',
            groups: groups,
            initialCount: configuredProperties.rules
              ? configuredProperties.rules.length
              : 1,
            groupFactory: groupFactory,
          },
          {
            element: {
              container: 'form-inline form-array rules-group',
            },
          },
        ),
      ],
    }),
  ];
  return answer;
}
