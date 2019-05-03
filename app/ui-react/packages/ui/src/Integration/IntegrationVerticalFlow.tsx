import classnames from 'classnames';
import * as React from 'react';
import { ButtonLink } from '../Layout';
import './IntegrationVerticalFlow.css';

export interface IIntegrationVerticalFlowProps {
  /**
   * A render prop that receives the expanded state of the container.
   * @see [props.expanded]{@link IIntegrationVerticalFlowState#expanded}
   */
  children(props: IIntegrationVerticalFlowState): any;
}

export interface IIntegrationVerticalFlowState {
  /**
   * The container expanded state.
   */
  expanded: boolean;
}

/**
 * A component to render an expandable container meant to be used to show an
 * integration's steps.
 * @see [children]{@link IIntegrationVerticalFlowProps#children}
 */
export class IntegrationVerticalFlow extends React.Component<
  IIntegrationVerticalFlowProps,
  IIntegrationVerticalFlowState
> {
  public static defaultProps = {
    disabled: false,
  };

  public state = {
    expanded:
      (localStorage.getItem('iec-vertical-flow-expanded') || 'y') === 'y',
  };

  constructor(props: IIntegrationVerticalFlowProps) {
    super(props);
    this.toggleExpanded = this.toggleExpanded.bind(this);
  }

  public toggleExpanded(): void {
    const expanded = !this.state.expanded;
    localStorage.setItem('iec-vertical-flow-expanded', expanded ? 'y' : 'n');
    this.setState({
      expanded,
    });
  }

  public render() {
    return (
      <div
        className={classnames('integration-vertical-flow', {
          'is-expanded': this.state.expanded,
        })}
      >
        <div className="integration-vertical-flow__body">
          {this.props.children(this.state)}
        </div>
        <div className="integration-vertical-flow__expand">
          <ButtonLink onClick={this.toggleExpanded} />
        </div>
      </div>
    );
  }
}
