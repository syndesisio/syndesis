import { DropdownKebab, ListViewItem, MenuItem } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailHistoryListViewItemProps {
  integrationVersion: string;
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
            <div>
              <DropdownKebab id="action2kebab" pullRight={true}>
                <MenuItem>
                  {this.props.i18nTextHistoryMenuReplaceDraft}
                </MenuItem>
                <MenuItem>{this.props.i18nTextHistoryMenuUnpublish}</MenuItem>
              </DropdownKebab>
            </div>
          }
          description={<span>{this.props.i18nTextLastPublished}</span>}
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
        <ListViewItem
          key={2}
          heading={
            <span>
              {<span>{this.props.i18nTextVersion}:</span>}{' '}
              {this.props.integrationVersion}
            </span>
          }
          actions={
            <div>
              <DropdownKebab id="action2kebab" pullRight={true}>
                <MenuItem>
                  {this.props.i18nTextHistoryMenuReplaceDraft}
                </MenuItem>
                <MenuItem>{this.props.i18nTextHistoryMenuUnpublish}</MenuItem>
              </DropdownKebab>
            </div>
          }
          description={<span>{this.props.i18nTextLastPublished}</span>}
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
