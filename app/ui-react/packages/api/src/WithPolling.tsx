import * as React from 'react';

export interface IWithPollingProps {
  polling: number;
  read: () => void;
  children(): any;
}

export class WithPolling extends React.Component<IWithPollingProps> {
  public pollingTimer?: number;

  public constructor(props: IWithPollingProps) {
    super(props);
    this.poller = this.poller.bind(this);
  }

  public async componentDidMount() {
    this.startPolling();
  }

  public async componentWillUnmount() {
    this.stopPolling();
  }

  public render() {
    return this.props.children();
  }

  public poller() {
    this.props.read();
  }

  public setPoller(func: () => void) {
    this.poller = func;
  }

  private startPolling() {
    this.stopPolling();
    if (this.props.polling) {
      this.pollingTimer = setInterval(this.poller, this.props.polling) as any;
    }
  }

  private stopPolling() {
    if (this.pollingTimer) {
      clearInterval(this.pollingTimer);
      this.pollingTimer = undefined;
    }
  }
}
