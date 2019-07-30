import { Gallery } from '@patternfly/react-core';
import * as React from 'react';

export class ConnectionsGrid extends React.Component {
  public render() {
    return <Gallery gutter={'sm'}>{this.props.children}</Gallery>;
  }
}
