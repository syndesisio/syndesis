import classnames from 'classnames';
import { OverlayTrigger, Tooltip } from 'patternfly-react';
import * as React from 'react';
import './IntegrationFlowStep.css';

export interface IIntegrationFlowStepProps {
  icon: string;
  showDetails: boolean;
  i18nTooltip: string;
  active?: boolean;
}

export class IntegrationFlowStep extends React.Component<
  IIntegrationFlowStepProps
> {
  public static defaultProps = {
    active: false,
  };

  public render() {
    const tooltip = (
      <Tooltip id={'integration-flow-step'}>{this.props.i18nTooltip}</Tooltip>
    );
    return (
      <div
        className={classnames('integration-flow-step', {
          'is-active': this.props.active,
        })}
      >
        <div className={'integration-flow-step__iconWrapper'}>
          <OverlayTrigger
            overlay={tooltip}
            placement="right"
            trigger={['hover', 'focus']}
            rootClose={false}
          >
            <div className={'integration-flow-step__icon'}>
              {this.props.icon}
            </div>
          </OverlayTrigger>
        </div>
        {this.props.showDetails && this.props.children}
      </div>
    );
  }
}
