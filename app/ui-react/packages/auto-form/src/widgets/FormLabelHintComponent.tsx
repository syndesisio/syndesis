import { Popover } from '@patternfly/react-core';
import { OutlinedQuestionCircleIcon } from '@patternfly/react-icons';
import * as React from 'react';

import './FormLabelHintComponent.css';

export interface IFormLabelHintComponentProps {
  labelHint: string;
}

export const FormLabelHintComponent: React.FunctionComponent<IFormLabelHintComponentProps> = ({ labelHint }) => {
  return (
    <Popover
      aria-label={labelHint}
      bodyContent={labelHint}
      className={'form-label-hint__popover'}
    >
      <OutlinedQuestionCircleIcon className="pf-u-ml-xs" data-testid={'tooltip'}/>
    </Popover>
  )
};
