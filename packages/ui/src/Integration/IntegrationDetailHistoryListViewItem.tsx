import { ListViewInfoItem, ListViewItem } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailHistoryListViewItemProps {
  i18nTextBtnEdit?: string;
  i18nTextBtnPublish?: string;
  i18nTextDraft?: string;
  i18nTextHistoryMenuReplaceDraft?: string;
  i18nTextHistoryMenuUnpublish?: string;
  i18nTextLastPublished?: string;
  i18nTextNoDescription?: string;
}

export class IntegrationDetailHistoryListViewItem extends React.Component<
  IIntegrationDetailHistoryListViewItemProps
> {
  public render() {
    return (
      <ListViewItem
        actions={<div className="form-group">Placeholder</div>}
        additionalInfo={[
          <ListViewInfoItem key={1}>Used by message</ListViewInfoItem>,
        ]}
        description={'Description'}
        heading="Name"
        hideCloseIcon={true}
        leftContent={null}
        stacked={false}
      />
    );
  }
}
