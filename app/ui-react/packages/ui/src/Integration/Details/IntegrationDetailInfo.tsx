import { Label } from 'patternfly-react';
import * as React from 'react';
import { IntegrationStatusDetail } from '../IntegrationStatusDetail';
import { IntegrationState } from '../models';
import './IntegrationDetailInfo.css';

export interface IIntegrationDetailInfoProps {
  name?: React.ReactNode;
  version?: number;
  currentState: IntegrationState;
  targetState: IntegrationState;
  monitoringValue?: string;
  monitoringCurrentStep?: number;
  monitoringTotalSteps?: number;
  monitoringLogUrl?: string;
  i18nProgressPending: string;
  i18nProgressStarting: string;
  i18nProgressStopping: string;
  i18nLogUrlText: string;
}

export class IntegrationDetailInfo extends React.PureComponent<
  IIntegrationDetailInfoProps
> {
  public render() {
    return (
      <div className="integration-detail-info">
        {this.props.name}
        <>
          {this.props.currentState === 'Pending' && (
            <IntegrationStatusDetail
              targetState={this.props.targetState}
              value={this.props.monitoringValue}
              currentStep={this.props.monitoringCurrentStep}
              totalSteps={this.props.monitoringTotalSteps}
              logUrl={this.props.monitoringLogUrl}
              i18nProgressPending={this.props.i18nProgressPending}
              i18nProgressStarting={this.props.i18nProgressStarting}
              i18nProgressStopping={this.props.i18nProgressStopping}
              i18nLogUrlText={this.props.i18nLogUrlText}
            />
          )}
          {this.props.currentState === 'Published' && this.props.version && (
            <div className="integration-detail-info__status">
              <span className="pficon pficon-ok integration-detail-info__status-icon" />
              Published version {this.props.version}
            </div>
          )}
          {this.props.currentState === 'Unpublished' && <Label>Stopped</Label>}
        </>
      </div>
    );
  }
}
