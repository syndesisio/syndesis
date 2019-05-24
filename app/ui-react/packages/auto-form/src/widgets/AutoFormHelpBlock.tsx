import { HelpBlock } from 'patternfly-react';
import * as React from 'react';

export interface IAutoFormHelpBlockProps {
  error?: string;
  description?: string;
}

export class AutoFormHelpBlock extends React.Component<
  IAutoFormHelpBlockProps
> {
  public render() {
    return (
      <HelpBlock>{this.props.error || this.props.description || ''}</HelpBlock>
    );
  }
}
