import { Icon, Spinner } from 'patternfly-react';
import * as React from 'react';

export interface IVirtualizationPublishStatusDetailProps {
  logUrl?: string;
  i18nPublishInProgress: string;
  i18nLogUrlText: string;
}

export class VirtualizationPublishStatusDetail extends React.Component<
  IVirtualizationPublishStatusDetailProps
> {
  public render() {
    return (
      <>
        <Spinner loading={true} inline={true} />
        {this.props.i18nPublishInProgress}
        {this.props.logUrl && (
          <a target="_blank" href={this.props.logUrl}>
            {this.props.i18nLogUrlText} <Icon name={'external-link'} />
          </a>
        )}
      </>
    );
  }
}
