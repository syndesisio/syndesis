import { Button } from 'patternfly-react';
import { ListViewItem } from 'patternfly-react';
import * as React from 'react';

export interface ICiCdListItemProps {
  onEditClicked: (name: string) => void;
  onRemoveClicked: (name: string) => void;
  i18nEditButtonText: string;
  i18nRemoveButtonText: string;
  /**
   * Text string for the number of integrations using this tag
   */
  i18nUsesText: string;
  /**
   * Environment name
   */
  name: string;
}

export class CiCdListItem extends React.Component<ICiCdListItemProps> {
  constructor(props: ICiCdListItemProps) {
    super(props);
    this.handleEditClicked = this.handleEditClicked.bind(this);
    this.handleRemoveClicked = this.handleRemoveClicked.bind(this);
  }
  public handleEditClicked() {
    this.props.onEditClicked(this.props.name);
  }
  public handleRemoveClicked() {
    this.props.onRemoveClicked(this.props.name);
  }
  public render() {
    return (
      <ListViewItem
        heading={this.props.name}
        description={this.props.i18nUsesText}
        additionalInfo={<span>&nbsp;{/* workaround */}</span>}
        actions={
          <div>
            <Button onClick={this.handleEditClicked}>
              {this.props.i18nEditButtonText}
            </Button>
            <Button onClick={this.handleRemoveClicked}>
              {this.props.i18nRemoveButtonText}
            </Button>
          </div>
        }
      />
    );
  }
}
