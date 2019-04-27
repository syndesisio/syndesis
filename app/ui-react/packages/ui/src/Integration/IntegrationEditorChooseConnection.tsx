import { Text, Title } from '@patternfly/react-core';
import { ListView } from 'patternfly-react';
import * as React from 'react';
import { Container } from '../Layout';

export interface IIntegrationEditorChooseConnection {
  /**
   * The main title of the content, shown before the connections.
   */
  i18nTitle: string;
  /**
   * The description of the content, shown before the connections.
   */
  i18nSubtitle: string;
}

/**
 * A component to render a list of connections, to be used in the integration
 * editor.
 *
 * @see [i18nTitle]{@link IIntegrationEditorChooseConnection#i18nTitle}
 * @see [i18nSubtitle]{@link IIntegrationEditorChooseConnection#i18nSubtitle}
 */
export class IntegrationEditorChooseConnection extends React.Component<
  IIntegrationEditorChooseConnection
> {
  public render() {
    return (
      <>
        <Container className="pf-u-my-md">
          <Title size="xl">{this.props.i18nTitle}</Title>
          <Text>{this.props.i18nSubtitle}</Text>
        </Container>
        <Container>
          <ListView>{this.props.children}</ListView>
        </Container>
      </>
    );
  }
}
