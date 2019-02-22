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
  integrationIsDraft?: boolean;
  integrationUpdatedAt?: Date;
  integrationVersion?: number;
  i18nTextBtnEdit?: string;
  i18nTextBtnPublish?: string;
  i18nTextDraft?: string;
  i18nTextHistory?: string;
  i18nTextHistoryMenuReplaceDraft?: string;
  i18nTextHistoryMenuUnpublish?: string;
  i18nTextLastPublished?: string;
  i18nTextTitle?: string;
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
            this.props.integrationIsDraft ? (
              <>{this.props.i18nTextDraft}</>
            ) : (
              <span>
                {<span>{this.props.i18nTextVersion}:</span>}{' '}
                {this.props.integrationVersion}
              </span>
            )
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
          additionalInfo={
            !this.props.integrationIsDraft
              ? [
                  <ListViewInfoItem key={1}>
                    {this.props.i18nTextLastPublished}
                    {this.props.integrationUpdatedAt}
                  </ListViewInfoItem>,
                ]
              : null
          }
          leftContent={
            !this.props.integrationIsDraft ? (
              <ListView.Icon
                type="pf"
                name="ok"
                size="xs"
                className="list-view-pf-icon-success"
              />
            ) : null
          }
          stacked={false}
        />
      </>
    );
  }
}
