import { Title } from '@patternfly/react-core';
import * as React from 'react';
import './IntegrationDetailEditableName.css';

export interface IIntegrationDetailEditableNameProps {
  name?: React.ReactNode;
}

export class IntegrationDetailEditableName extends React.PureComponent<
  IIntegrationDetailEditableNameProps
> {
  public render() {
    return (
      <>
        {this.props.name && (
          <Title size="lg" className="integration-detail-editable-name pf-u-mr-lg">
            {this.props.name}
          </Title>
        )}
      </>
    );
  }
}
