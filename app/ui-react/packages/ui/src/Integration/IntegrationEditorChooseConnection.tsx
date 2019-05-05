import * as React from 'react';
import { ConnectionsGrid } from '../Connection';
import { Container } from '../Layout';

/**
 * A component to render a list of connections, to be used in the integration
 * editor.
 */
export class IntegrationEditorChooseConnection extends React.Component {
  public render() {
    return (
      <Container>
        <ConnectionsGrid>{this.props.children}</ConnectionsGrid>
      </Container>
    );
  }
}
