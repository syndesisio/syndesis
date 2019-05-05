import { ListView } from 'patternfly-react';
import * as React from 'react';
import { Container } from '../Layout';

/**
 * A component to render a list of actions, to be used in the integration
 * editor.
 */
export class IntegrationEditorChooseAction extends React.Component {
  public render() {
    return (
      <Container>
        <ListView>{this.props.children}</ListView>
      </Container>
    );
  }
}
