import { MessageDialog } from 'patternfly-react';
import * as React from 'react';

export interface IDialogProps {
  body: any;
  footer: any;
  onHide: () => void;
  title: string;
}

export class Dialog extends React.Component<IDialogProps> {
  public render() {
    return (
      <MessageDialog
        title={this.props.title}
        primaryContent={this.props.body}
        footer={this.props.footer}
        show={true}
        onHide={this.props.onHide}
      />
    );
  }
}
