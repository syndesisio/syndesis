import * as React from 'react';

import { IntegrationDetailEditableName } from './IntegrationDetailEditableName';

import './IntegrationDetailInfo.css';

export interface IIntegrationDetailInfoProps {
  name?: string;
  version?: number;
}

export class IntegrationDetailInfo extends React.PureComponent<
  IIntegrationDetailInfoProps
> {
  public render() {
    return (
      <div className="integration-detail-info">
        <IntegrationDetailEditableName name={this.props.name} />
        {this.props.version ? (
          <>
            <span className="pficon pficon-ok" />
            &nbsp;Published version {this.props.version}
          </>
        ) : (
          'Stopped'
        )}
      </div>
    );
  }
}
