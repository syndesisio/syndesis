import { HelpBlock } from 'patternfly-react';
import * as React from 'react';

export interface IAutoFormHelpBlockProps {
  error?: string;
  description?: string;
}

export const AutoFormHelpBlock: React.FunctionComponent<
  IAutoFormHelpBlockProps
> = props => {
  return <HelpBlock>{props.error || props.description || ''}</HelpBlock>;
};
