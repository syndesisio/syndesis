import { Popover } from '@patternfly/react-core';
import { OutlinedQuestionCircleIcon } from '@patternfly/react-icons';
import * as React from 'react';
import { TextButton } from './TextButton';

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
      <TextButton className={'form-label-hint__text-button'}>
        <OutlinedQuestionCircleIcon className="pf-u-ml-xs" />
      </TextButton>
    </Popover>
  )
};
