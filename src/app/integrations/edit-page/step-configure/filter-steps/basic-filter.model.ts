import {
  DynamicInputModel,
  DynamicSelectModel,
  DynamicFormArrayModel,
  DynamicFormGroupModel,
} from '@ng2-dynamic-forms/core';
import { Observable } from 'rxjs/Observable';

export const BASIC_FILTER_MODEL = [

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
          value: 'AND',
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
          initialCount: 1,
          groupFactory: () => {
            return [
              new DynamicInputModel(
                {
                  id: 'path',
                  maxLength: 51,
                  placeholder: 'Status.Text',
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
                  placeholder: 'Keywords...',
                },
                {
                  grid: {
                    container: 'input-group col-xs-4 keywords',
                  },
                },
              ),
            ];
          },
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
