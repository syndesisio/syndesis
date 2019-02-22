import {
  Button,
  DropdownKebab,
  ListView,
  ListViewInfoItem,
  ListViewItem,
  MenuItem,
} from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailHistoryListViewItemProps {
  integrationCreatedAt?: Date;
  integrationIsDraft?: boolean;
  integrationName?: string;
  integrationUpdatedAt?: Date;
  integrationVersion?: number;
  i18nTextBtnEdit?: string;
  i18nTextBtnPublish?: string;
  i18nTextDraft?: string;
  i18nTextHistory?: string;
  i18nTextHistoryMenuReplaceDraft?: string;
  i18nTextHistoryMenuUnpublish?: string;
  i18nTextLastPublished?: string;
  i18nTextNoDescription?: string;
  i18nTextTitle: string;
  i18nTextVersion: string;
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
            this.props.integrationIsDraft ? (
              <>
                <Button>{this.props.i18nTextBtnPublish}</Button>
                <Button>{this.props.i18nTextBtnEdit}</Button>
              </>
            ) : (
              <DropdownKebab id="action2kebab" pullRight={true}>
                <MenuItem>
                  {this.props.i18nTextHistoryMenuReplaceDraft}
                </MenuItem>
                <MenuItem>{this.props.i18nTextHistoryMenuUnpublish}</MenuItem>
              </DropdownKebab>
            )
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
              size="xs"
              className="list-view-pf-icon-success"
            />
          }
          stacked={false}
          i18nTextBtnEdit={this.props.i18nTextBtnEdit}
          i18nTextBtnPublish={this.props.i18nTextBtnPublish}
          i18nTextHistoryMenuReplaceDraft={
            this.props.i18nTextHistoryMenuReplaceDraft
          }
          i18nTextHistoryMenuUnpublish={this.props.i18nTextHistoryMenuUnpublish}
          i18nTextLastPublished={this.props.i18nTextLastPublished}
          i18nTextTitle={this.props.i18nTextTitle}
          i18nTextVersion={this.props.i18nTextVersion}
        />
      </>
    );
  }
}
