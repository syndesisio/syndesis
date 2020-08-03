import { Label } from '@patternfly/react-core';
import { OkIcon } from '@patternfly/react-icons';
import {
  global_default_color_100,
  global_success_color_100,
} from '@patternfly/react-tokens';
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
              <span
                className={'integration-detail-info__status-icon'}
              >
                <OkIcon style={{ color: global_success_color_100.value }} />
              </span>
              Published version {this.props.version}
            </div>
          )}
          {this.props.currentState === 'Unpublished' && (
            <Label style={{ background: global_default_color_100.value }}>
              Stopped
            </Label>
          )}
        </>
      </div>
    );
  }
}
