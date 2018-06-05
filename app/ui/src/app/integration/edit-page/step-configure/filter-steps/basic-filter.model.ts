import {
  DynamicInputModel,
  DynamicSelectModel,
  DynamicFormArrayModel,
  DynamicFormArrayGroupModel,
  DynamicFormGroupModel
} from '@ng-dynamic-forms/core';
import { Observable, of } from 'rxjs';

import {
  BasicFilter,
  Op,
  getDefaultOps,
  convertOps
} from './filter.interface';

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
      break;
    default:
      break;
  }
  return false;
}

export function createBasicFilterModel(
  configuredProperties: BasicFilter,
  ops: Array<Op> = [],
  paths: Array<string> = []
) {
  if (!ops || !ops.length) {
    ops = getDefaultOps();
  } else {
    ops = convertOps(ops);
  }

  // inline this function since we use it in a couple places
  function groupFactory(rule?) {
    return [
      new DynamicInputModel(
        {
          id: 'path',
          maxLength: 51,
          required: true,
          hint: 'The data you want to evaluate',
          validators: {
            required: null
          },
          errorMessages: {
            required: 'Object property name is required'
          },
          //placeholder: paths.length ? paths[0] : 'Field Name',
          placeholder: 'Property name',
          value: rule ? rule.path : undefined,
          list: paths
          //suffix: 'Browse...', // This is just a suffix; this whole field needs to change
        },
        {
          element: {
            container: 'form-group col-xs-3'
          }
        }
      ),
      new DynamicSelectModel<string>(
        {
          id: 'op',
          hint: 'Must meet this condition',
          value: rule ? rule.op : 'contains',
          options: of(<any>ops)
        },
        {
          element: {
            container: 'form-group col-xs-3',
            label: 'control-label'
          }
        }
      ),
      new DynamicInputModel(
        {
          hint: 'For this value',
          id: 'value',
          value: rule ? rule.value : undefined,
          placeholder: 'Keywords'
        },
        {
          grid: {
            container: 'col-xs-4 keywords'
          }
        }
      )
    ];
  }
  let groups = undefined;
  let rules = undefined;
  // build up the form array from the incoming values (if any)
  if (configuredProperties && configuredProperties.rules) {
    // TODO hackity hack
    if (typeof configuredProperties.rules === 'string') {
      rules = JSON.parse(<any>configuredProperties.rules);
    } else {
      rules = configuredProperties.rules;
    }
    groups = [];
    for (const rule of rules) {
      groups.push(
        new DynamicFormArrayGroupModel(undefined, groupFactory(rule))
      );
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
            options: of([
              {
                label: 'ALL of the following',
                value: 'AND'
              },
              {
                label: 'ANY of the following',
                value: 'OR'
              }
            ]),
            value: configuredProperties.predicate || 'AND'
          },
          {
            element: {
              label: 'control-label pull-left'
            },
            grid: {
              control: 'col-xs-3 match-select'
            }
          }
        )
      ]
    }),

    new DynamicFormGroupModel({
      id: 'rulesGroup',
      group: [
        new DynamicFormArrayModel(
          {
            id: 'rulesFormArray',
            groups: groups,
            initialCount: rules ? rules.length : 1,
            groupFactory: groupFactory
          },
          {
            element: {
              container: 'form-inline form-array rules-group'
            }
          }
        )
      ]
    })
  ];
  return answer;
}
