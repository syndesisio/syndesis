import { ListView } from 'patternfly-react';
import * as React from 'react';
import './IntegrationEditorStepsList.css';

export class IntegrationEditorStepsList extends React.Component<{}> {
  public render() {
    return (
      <ListView className={'integration-editor-steps-list'}>
        {this.props.children}
      </ListView>
    );
  }
}
