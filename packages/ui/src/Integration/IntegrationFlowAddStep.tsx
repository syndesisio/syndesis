import * as H from 'history';
import { Overlay, Popover } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import './IntegrationFlowAddStep.css';

export interface IIntegrationFlowAddStepProps {
  showDetails: boolean;
  forceTooltip?: boolean;
  addStepHref?: H.LocationDescriptor;
  i18nAddStep?: string;
  addConnectionHref?: H.LocationDescriptor;
  i18nAddConnection?: string;
}

export interface IIntegrationFlowAddStepState {
  showTooltip: boolean;
}

export class IntegrationFlowAddStep extends React.Component<
  IIntegrationFlowAddStepProps,
  IIntegrationFlowAddStepState
> {
  public static defaultProps = {
    active: false,
  };

  public state = {
    forceTooltip: false,
    showTooltip: false,
  };

  public containerRef = React.createRef<HTMLDivElement>();

  constructor(props: IIntegrationFlowAddStepProps) {
    super(props);
    this.showTooltip = this.showTooltip.bind(this);
    this.hideTooltip = this.hideTooltip.bind(this);
  }

  public showTooltip() {
    this.setState({
      showTooltip: true,
    });
  }

  public hideTooltip() {
    this.setState({
      showTooltip: false,
    });
  }

  public render() {
    return (
      <div
        className={'integration-flow-add-step'}
        onMouseEnter={this.showTooltip}
        onMouseLeave={this.hideTooltip}
        ref={this.containerRef}
      >
        <div className={'integration-flow-add-step__iconWrapper'}>
          <div className={'integration-flow-add-step__icon'}>
            <i className="fa fa-plus-square" />
          </div>
        </div>
        {this.props.showDetails && this.props.children}
        <Overlay
          placement="bottom"
          rootClose={false}
          show={this.props.forceTooltip || this.state.showTooltip}
          target={this.containerRef.current}
          container={this}
        >
          <Popover
            id={'integration-flow-add-step'}
            className={'integration-flow-add-step__links'}
          >
            {this.props.addStepHref && this.props.i18nAddStep && (
              <Link to={this.props.addStepHref}>{this.props.i18nAddStep}</Link>
            )}
            {this.props.addConnectionHref && this.props.i18nAddConnection && (
              <Link to={this.props.addConnectionHref}>
                {this.props.i18nAddConnection}
              </Link>
            )}
          </Popover>
        </Overlay>
      </div>
    );
  }
}
