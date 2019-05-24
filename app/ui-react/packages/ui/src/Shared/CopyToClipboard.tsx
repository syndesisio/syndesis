import { ClipboardCopy } from '@patternfly/react-core';
import * as React from 'react';

export interface ICopyToClipboardProps {
  children: string;
}

export class CopyToClipboard extends React.Component<ICopyToClipboardProps> {
  constructor(props: ICopyToClipboardProps) {
    super(props);
  }

  public render() {
    return <ClipboardCopy>{this.props.children}</ClipboardCopy>;
  }
}
