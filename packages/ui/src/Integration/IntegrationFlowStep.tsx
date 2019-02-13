import classnames from 'classnames';
import { OverlayTrigger, Tooltip } from 'patternfly-react';
import * as React from 'react';
import './IntegrationFlowStep.css';

export interface IIntegrationFlowStepProps {
  /**
   * The icon to show. Can be anything but must fit a circle of 55px of diameter.
   */
  icon: any;
  /**
   * Set to true to render the extended details for the step. Usually set to match
   * the expanded state of the parent container.
   */
  showDetails: boolean;
  /**
   * The text to show on the tooltip that opens when hovering with the mouse on
   * the icon.
   */
  i18nTooltip: string;
  /**
   * Set to true to set this step as active. This will highlight the icon circle.
   */
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
    const icon = (
      <div className={'integration-flow-step__icon'}>{this.props.icon}</div>
    );
    return (
      <div
        className={classnames('integration-flow-step', {
          'is-active': this.props.active,
          'is-expanded': this.props.showDetails,
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
