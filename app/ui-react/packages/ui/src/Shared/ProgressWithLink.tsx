import { Progress } from '@patternfly/react-core';
import { ExternalLinkAltIcon } from '@patternfly/react-icons';
import * as React from 'react';
import './ProgressWithLink.css';

export interface IProgressWithLinkProps {
  value: string;
  currentStep: number;
  totalSteps: number;
  logUrl?: string;
  i18nLogUrlText: string;
}

export class ProgressWithLink extends React.PureComponent<IProgressWithLinkProps> {
  public render() {
    return (
      <div className="progress-link">
        <div className="progress-link__row">
          <div
            className="progress-link__status"
            data-testid={'progress-with-link-value'}
          >
            {this.props.value} ( {this.props.currentStep} /{' '}
            {this.props.totalSteps} )
          </div>
          {this.props.logUrl && (
            <span className="progress-link__link">
              <a
                data-testid={'progress-with-link-log-url'}
                target="_blank"
                href={this.props.logUrl}
                rel="noopener noreferrer"
              >
                {this.props.i18nLogUrlText}{' '}
                <ExternalLinkAltIcon className="progress-link__link-icon" />
              </a>
            </span>
          )}
        </div>
        <Progress
          value={this.props.currentStep}
          max={this.props.totalSteps}
          style={{
            height: 6,
          }}
        />
      </div>
    );
  }
}
