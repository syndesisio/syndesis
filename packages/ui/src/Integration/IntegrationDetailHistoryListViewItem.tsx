import {
  DropdownKebab,
  ListView,
  ListViewInfoItem,
  ListViewItem,
  MenuItem,
} from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailHistoryListViewItemProps {
  integrationUpdatedAt: string;
  integrationVersion: number;
  i18nTextHistoryMenuReplaceDraft?: string;
  i18nTextHistoryMenuUnpublish?: string;
  i18nTextLastPublished?: string;
  i18nTextVersion?: string;
}

export class IntegrationDetailHistoryListViewItem extends React.Component<
  IIntegrationDetailHistoryListViewItemProps
> {
  public render() {
    return (
      <>
        <ListViewItem
          key={1}
          heading={
            <span>
              {<span>{this.props.i18nTextVersion}:</span>}{' '}
              {this.props.integrationVersion}
            </span>
          }
          actions={
            <DropdownKebab id="historyactions" pullRight={true}>
              <MenuItem>{this.props.i18nTextHistoryMenuReplaceDraft}</MenuItem>
              <MenuItem>{this.props.i18nTextHistoryMenuUnpublish}</MenuItem>
            </DropdownKebab>
          }
          additionalInfo={[
            <ListViewInfoItem key={1}>
              {this.props.i18nTextLastPublished}
              {this.props.integrationUpdatedAt}
            </ListViewInfoItem>,
          ]}
          leftContent={
            <ListView.Icon
              type="pf"
              name="ok"
              size="sm"
              className="list-view-pf-icon-success"
            />
          }
          stacked={false}
        />
      </>
    );
  }
}
