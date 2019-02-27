import { ListView } from 'patternfly-react';
import * as React from 'react';

export class IntegrationEditorStepsList extends React.Component<{}> {
  public render() {
    return (
      <ListView style={{ background: 'transparent' }}>
        {this.props.children}
      </ListView>
    );
  }
}
