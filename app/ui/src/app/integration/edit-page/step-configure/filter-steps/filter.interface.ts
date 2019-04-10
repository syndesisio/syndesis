export interface BasicFilter {
  type: string;
  predicate: any;
  simple?: string;
  rules?: Rule[];
}

export interface Rule {
  path: string;
  op?: string;
  value: string;
}

export interface Op {
  label: string;
  operator?: string;
  value?: string;
}

export function convertOps(ops: Array<Op>): Array<Op> {
  const answer = [];
  for (const op of ops) {
    // guard against blank label
    const label = op.label || op.value || op.operator;
    const newOp = {
      label: label,
      value: op.value || op.operator
    };
    answer.push(newOp);
  }
  return answer;
}

export function getDefaultOps(): Array<Op> {
  return [
    {
      label: 'Contains',
      value: 'contains'
    },
    {
      label: 'Does Not Contain',
      value: 'not contains'
    },
    {
      label: 'Matches Regex',
      value: 'regex'
    },
    {
      label: 'Does Not Match Regex',
      value: 'not regex'
    },
    {
      label: 'Starts With',
      value: 'starts with'
    },
    {
      label: 'Ends With',
      value: 'ends with'
    }
  ];
}
