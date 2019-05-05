import { Text } from '@patternfly/react-core';
import * as React from 'react';
import { Container } from '../Layout';

export interface IIntegrationEditorNothingToConfigureProps {
  /**
   * The internationalized alert to display.
   */
  i18nAlert: string;
}

/**
 * A component to render an alert for unconfigurable actions.
 * @see [i18nAlert]{@link IIntegrationEditorNothingToConfigureProps#i18nAlert}
 */
export class IntegrationEditorNothingToConfigure extends React.Component<
  IIntegrationEditorNothingToConfigureProps
> {
  public render() {
    return (
      <Container>
        <div className="row row-cards-pf">
          <div className="card-pf">
            <div className="card-pf-body">
              <Container>
                <Text className="alert alert-info">
                  <span className="pficon pficon-info" />
                  {this.props.i18nAlert}
                </Text>
              </Container>
            </div>
          </div>
        </div>
      </Container>
    );
  }
}
