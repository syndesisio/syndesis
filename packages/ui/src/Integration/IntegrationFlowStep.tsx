import classnames from 'classnames';
import { OverlayTrigger, Tooltip } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import './IntegrationFlowStep.css';

export interface IIntegrationFlowStepProps {
  icon: any;
  showDetails: boolean;
  i18nTooltip: string;
  active?: boolean;
  href?: string;
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
    const baseIcon = (
      <div className={'integration-flow-step__icon'}>{this.props.icon}</div>
    );
    const icon = !this.props.href ? (
      baseIcon
    ) : (
      <Link to={this.props.href}>{baseIcon}</Link>
    );
    return (
      <div
        className={classnames('integration-flow-step', {
          'is-active': this.props.active,
        })}
      >
        {this.props.showDetails ? (
          <>
            <div className={'integration-flow-step__iconWrapper'}>{icon}</div>
            {this.props.children}
          </>
        ) : (
          <div className={'integration-flow-step__iconWrapper'}>
            <OverlayTrigger
              overlay={tooltip}
              placement="right"
              trigger={['hover', 'focus']}
              rootClose={false}
            >
              {icon}
            </OverlayTrigger>
          </div>
        )}
      </div>
    );
  }
}
