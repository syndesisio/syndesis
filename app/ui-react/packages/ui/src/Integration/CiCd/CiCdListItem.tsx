import { Button, ListViewInfoItem, ListViewItem } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../helpers';

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
        data-testid={`cicd-list-item-${toValidHtmlId(
          this.props.name
        )}-list-item`}
        heading={this.props.name}
        description={''}
        additionalInfo={[
          <ListViewInfoItem key={0}>
            <i>{this.props.i18nUsesText}</i>
          </ListViewInfoItem>,
        ]}
        actions={
          <div>
            <Button
              data-testid={'cicd-list-item-create-button'}
              onClick={this.handleEditClicked}
            >
              {this.props.i18nEditButtonText}
            </Button>
            <Button
              data-testid={'cicd-list-item-remove-button'}
              onClick={this.handleRemoveClicked}
            >
              {this.props.i18nRemoveButtonText}
            </Button>
          </div>
        }
      />
    );
  }
}
