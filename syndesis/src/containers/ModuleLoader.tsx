import { Loader, UnrecoverableError } from '@syndesis/ui';
import * as React from 'react';
import { LoadingComponentProps } from 'react-loadable';

export class ModuleLoader extends React.Component<LoadingComponentProps> {
  public render() {
    if (this.props.error || this.props.timedOut) {
      console.error(this.props.error); // tslint:disable-line
      return <UnrecoverableError />;
    } else if (this.props.pastDelay) {
      return <Loader />;
    }
    return null;
  }
}
