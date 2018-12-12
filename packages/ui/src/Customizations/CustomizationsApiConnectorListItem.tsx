import { ListView } from 'patternfly-react';
import * as React from 'react';

export interface ICustomizationsApiConnectorListItemProps {
  apiConnectorDescription?: string;
  apiConnectorId: string;
  apiConnectorName: string;
  apiConnectorIcon?: string;
  i18nDelete: string;
  i18nUsedByMessage: string;
  onDelete: (apiConnectorId: string) => void;
  usedBy: number;
}

export class CustomizationsApiConnectorListItem extends React.Component<
  ICustomizationsApiConnectorListItemProps
> {
  public render() {
    return (
      <ListView.Item
        actions={
          <div>
            <button
              disabled={this.props.usedBy === 0}
              onClick={this.handleDelete}
            >
              {this.props.i18nDelete}
            </button>
          </div>
        }
        additionalInfo={
          <ListView.InfoItem>
            <strong>{this.props.i18nUsedByMessage}</strong>
          </ListView.InfoItem>
        }
        description={
          this.props.apiConnectorDescription
            ? this.props.apiConnectorDescription
            : ''
        }
        heading={this.props.apiConnectorName}
        hideCloseIcon={true}
        leftContent={
          this.props.apiConnectorIcon ? <ListView.Icon name={'gear'} /> : null
        }
        stacked={false}
      />
    );
  }

  private handleDelete = () => {
    this.props.onDelete(this.props.apiConnectorId);
  };
}
