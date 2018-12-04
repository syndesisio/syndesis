import { Icon, ProgressBar } from 'patternfly-react';
import * as React from 'react';

import './IntegrationProgress.css';

export interface IIntegrationProgressProps {
  value: string;
  currentStep: number;
  totalSteps: number;
  logUrl?: string;
  i18nLogUrlText: string;
}

export class IntegrationProgress extends React.PureComponent<
  IIntegrationProgressProps
> {
  public render() {
    return (
      <div className="integration-progress">
        <div>
          <i data-testid="integration-progress-value">
            {this.props.value} ( {this.props.currentStep} /{' '}
            {this.props.totalSteps} )
          </i>
          {this.props.logUrl && (
            <span data-testid="deployment-log-link" className="pull-right">
              <a target="_blank" href={this.props.logUrl}>
                {this.props.i18nLogUrlText} <Icon name={'external-link'} />
              </a>
            </span>
          )}
        </div>
        <ProgressBar
          now={this.props.currentStep}
          max={this.props.totalSteps}
          style={{
            height: 6,
          }}
        />
      </div>
    );
  }
}
