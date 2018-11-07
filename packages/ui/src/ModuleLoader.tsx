import { Spinner } from 'patternfly-react';
import * as React from 'react';
import { LoadingComponentProps } from 'react-loadable';

import './ModuleLoader.css';
import { UnrecoverableError } from './UnrecoverableError';

export class ModuleLoader extends React.Component<LoadingComponentProps> {
  public render() {
    if (this.props.error || this.props.timedOut) {
      return <UnrecoverableError />;
    } else if (this.props.pastDelay) {
      return (
        <div className={'ModuleLoader'}>
          <Spinner loading={true} size={'lg'} />
        </div>
      );
    }
    return null;
  }
}
