import { Text } from '@patternfly/react-core';
import * as React from 'react';
import { PageSection } from '../../Layout';

export interface IIntegrationDetailDescriptionProps {
  description: React.ReactNode;
}

export class IntegrationDetailDescription extends React.PureComponent<
  IIntegrationDetailDescriptionProps
> {
  public render() {
    return (
      <PageSection>
        <Text>{this.props.description}</Text>
      </PageSection>
    );
  }
}
