import { Gallery } from '@patternfly/react-core';
import * as React from 'react';

export class ConnectionsGrid extends React.Component {
  public render() {
    return <Gallery hasGutter={true}>{this.props.children}</Gallery>;
  }
}
