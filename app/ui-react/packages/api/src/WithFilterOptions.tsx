import { DataShape, FilterOptions, Op } from '@syndesis/models';
import * as React from 'react';
import { IFetchState } from './Fetch';
import { SyndesisFetch } from './SyndesisFetch';

export interface IWithFilterOptionsProps {
  dataShape: DataShape;
  includeNoSelectionOption?: boolean;
  children(props: IFetchState<FilterOptions>): any;
}

function getDefaultOps() {
  return [
    {
      label: 'equals',
      operator: '==',
    },
    {
      label: 'equals (ignores case)',
      operator: '=~',
    },
    {
      label: 'not equals',
      operator: '!=',
    },
    {
      label: 'less than',
      operator: '<',
    },
    {
      label: 'less than or equal',
      operator: '<=',
    },
    {
      label: 'greater than',
      operator: '>',
    },
    {
      label: 'greater than or equal',
      operator: '>=',
    },
    {
      label: 'contains',
      operator: 'contains',
    },
    {
      label: 'contains (ignore case)',
      operator: '~~',
    },
    {
      label: 'not contains',
      operator: 'not contains',
    },
    {
      label: 'matches',
      operator: 'regex',
    },
    {
      label: 'not matches',
      operator: 'not regex',
    },
    {
      label: 'in',
      operator: 'in',
    },
    {
      label: 'not in',
      operator: 'not in',
    },
  ];
}

export interface IOp {
  label: string;
  operator?: string;
  value?: string;
}

function convertOps(ops: Op[], includeNoSelectionOption: boolean): Op[] {
  const answer = [];

  if (includeNoSelectionOption) {
    const noSelection = { label: 'Select an operator...', value: '' } as IOp;
    answer.push(noSelection);
  }

  for (const op of ops) {
    // guard against blank label
    const label = op.label || (op as IOp).value || op.operator;
    const value = op.label === 'not equals' ? '!=' : (op as IOp).value || op.operator;
    const newOp = {
      label,
      value
    } as IOp;
    answer.push(newOp);
  }
  return answer;
}

export class WithFilterOptions extends React.Component<
  IWithFilterOptionsProps
> {
  public render() {
    const defaultValue = {
      ops: [],
      paths: [],
    };
    return (
      <SyndesisFetch<FilterOptions>
        body={this.props.dataShape}
        defaultValue={defaultValue}
        method={'POST'}
        url={`/integrations/filters/options`}
      >
        {({ response }) => {
          const ops = response.data.ops!.length
            ? response.data.ops!
            : getDefaultOps();
          const data = {
            ops: convertOps(ops, this.props.includeNoSelectionOption || false),
            paths: response.data.paths || [],
          };
          return this.props.children({
            ...response,
            data,
          });
        }}
      </SyndesisFetch>
    );
  }
}
