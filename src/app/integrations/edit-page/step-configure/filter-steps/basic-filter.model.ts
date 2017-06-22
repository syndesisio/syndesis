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
          id: 'matchSelect',
          label: 'Continue only if incoming data match ',
          options: Observable.of([
            {
              label: 'ALL of the following',
              value: 'all',
            },
            {
              label: 'ANY of the following',
              value: 'any',
            },
          ]),
          value: 'all',
        },
        {
          element: {
            label: 'control-label pull-left',
          },
          grid: {
            control: 'col-xs-3',
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
          initialCount: 5,
          groupFactory: () => {
            return [
              new DynamicInputModel(
                {
                  id: 'dataToBeFiltered',
                  maxLength: 51,
                  placeholder: 'Status.Text',
                  suffix: 'Browse...', // This is just a suffix; this whole field needs to change
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
                  id: 'conditions',
                  options: Observable.of([
                    {
                      label: 'Contains',
                      value: 'contains',
                    },
                    {
                      label: 'Does Not Contain',
                      value: 'does-not-contain',
                    },
                    {
                      label: 'Matches Regex',
                      value: 'matches-regex',
                    },
                    {
                      label: 'Does Not Match Regex',
                      value: 'does-not-match-regex',
                    },
                    {
                      label: 'Starts With',
                      value: 'starts-with',
                    },
                    {
                      label: 'Ends With',
                      value: 'ends-with',
                    },
                  ]),
                  value: 'contains',
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
                  id: 'valueToCheckFor',
                  placeholder: 'e.g. Red Hat',
                },
                {
                  grid: {
                    container: 'input-group',
                  },
                },
              ),
            ];
          },
        },
        {
          element: {
            container: 'form-inline form-array',
          },
        },
      ),
    ],
  }),
];
