import { ListViewItem } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../helpers';

export interface IOAuthAppListItemProps {
  id: string;
  configured: boolean;
  children: React.ReactNode;
  expanded: boolean;
  icon: string;
  i18nNotConfiguredText: string;
  name: string;
}

export class OAuthAppListItem extends React.Component<IOAuthAppListItemProps> {
  constructor(props: IOAuthAppListItemProps) {
    super(props);
  }
  public render() {
    return (
      <ListViewItem
        data-testid={`o-auth-app-list-item-${toValidHtmlId(
          this.props.name
        )}-list-item`}
        key={this.props.id}
        hideCloseIcon={true}
        initExpanded={this.props.expanded}
        heading={this.props.name}
        leftContent={
          <img
            className={'list-pf-icon list-pf-icon-small'}
            src={this.props.icon}
          />
        }
        description={''}
        additionalInfo={[
          !this.props.configured && (
            <i key={0}>{this.props.i18nNotConfiguredText}</i>
          ),
        ]}
      >
        {this.props.children}
      </ListViewItem>
    );
  }
}
