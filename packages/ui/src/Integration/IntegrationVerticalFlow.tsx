import classnames from 'classnames';
import * as React from 'react';

import './IntegrationVerticalFlow.css';

export interface IIntegrationVerticalFlowProps {
  disabled?: boolean;
  children(props: IIntegrationVerticalFlowState): any;
}

export interface IIntegrationVerticalFlowState {
  expanded: boolean;
}

export class IntegrationVerticalFlow extends React.Component<
  IIntegrationVerticalFlowProps,
  IIntegrationVerticalFlowState
> {
  public static defaultProps = {
    disabled: false,
  };

  public state = {
    expanded: localStorage.getItem('iec-vertical-flow-expanded') === 'y',
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
          'is-disabled': this.props.disabled,
          'is-expanded': this.state.expanded,
        })}
      >
        <div className="integration-vertical-flow__body">
          {this.props.children(this.state)}
        </div>
        <div className="integration-vertical-flow__expand">
          <button className="btn btn-default" onClick={this.toggleExpanded} />
        </div>
      </div>
    );
  }
}
