import * as React from 'react';
import { IChangeEvent } from './WithServerEvents';

export interface IWithChangeListenerProps {
  filter: (change: IChangeEvent) => boolean;
  read: () => void;
  registerChangeListener: (listener: (event: IChangeEvent) => void) => void;
  unregisterChangeListener: (listener: (event: IChangeEvent) => void) => void;
  children(): any;
}

export class WithChangeListener extends React.Component<
  IWithChangeListenerProps
> {
  public constructor(props: IWithChangeListenerProps) {
    super(props);
    this.changeListener = this.changeListener.bind(this);
  }

  public render() {
    return this.props.children();
  }

  public async componentDidMount() {
    this.props.registerChangeListener(this.changeListener);
  }

  public async componentWillUnmount() {
    this.props.unregisterChangeListener(this.changeListener);
  }

  public changeListener(event: IChangeEvent) {
    if (this.props.filter(event)) {
      this.props.read();
    }
  }
}
