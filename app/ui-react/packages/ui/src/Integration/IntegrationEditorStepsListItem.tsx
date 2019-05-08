import { ListView } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationEditorStepsListItemProps {
  stepName: string;
  stepDescription: string;
  additionalInfo: any;
  actions: any;
  icon: any;
}

export class IntegrationEditorStepsListItem extends React.Component<
  IIntegrationEditorStepsListItemProps
> {
  public render() {
    return (
      <ListView.Item
        actions={this.props.actions}
        heading={this.props.stepName}
        description={this.props.stepDescription}
        additionalInfo={this.props.additionalInfo}
        leftContent={this.props.icon}
        stacked={true}
        hideCloseIcon={true}
      />
    );
  }
}
