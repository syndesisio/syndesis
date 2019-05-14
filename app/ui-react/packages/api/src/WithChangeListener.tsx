import { debounce } from '@syndesis/utils';
import * as React from 'react';
import { IChangeEvent } from './WithServerEvents';

export interface IWithChangeListenerProps {
  filter: (change: IChangeEvent) => boolean;
  read: () => void;
  disableDebounce?: boolean;
  debounceWait?: number;
  registerChangeListener: (listener: (event: IChangeEvent) => void) => void;
  unregisterChangeListener: (listener: (event: IChangeEvent) => void) => void;
  children(): any;
}

export class WithChangeListener extends React.Component<
  IWithChangeListenerProps
> {
  private readonly debouncedRead: () => void;
  public constructor(props: IWithChangeListenerProps) {
    super(props);
    this.changeListener = this.changeListener.bind(this);
    this.read = this.read.bind(this);
    this.debouncedRead = this.props.disableDebounce
      ? this.read
      : debounce(this.read, this.props.debounceWait || 250, false);
  }

  public read() {
    this.props.read();
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
      this.debouncedRead();
    }
  }
}
