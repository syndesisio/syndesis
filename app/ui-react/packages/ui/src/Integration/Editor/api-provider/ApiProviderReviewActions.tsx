import { ListView } from 'patternfly-react';
import * as React from 'react';

export interface IApiProviderReviewActionsProps {
  /**
   * The title
   */
  i18nTitle?: string;
}

export class ApiProviderReviewActions extends React.Component<
  IApiProviderReviewActionsProps
> {
  public render() {
    return (
      <ListView
        id="listView--listItemVariants"
        className="listView--listItemVariants"
      >
        <ListView.Item
          id="item1"
          className="listViewItem--listItemVariants"
          key="item1"
          description="Expandable item with description, additional items and actions"
          heading="Event One"
          checkboxInput={<input type="checkbox" />}
          leftContent={<ListView.Icon name="plane" />}
          additionalInfo={[
            <ListView.InfoItem key="1">Item 1</ListView.InfoItem>,
            <ListView.InfoItem key="2" />,
          ]}
          stacked={false}
        >
          Expanded Content
        </ListView.Item>
        <ListView.Item
          key="item2"
          leftContent={<ListView.Icon size="lg" name="plane" />}
          heading={
            <span>
              This is EVENT One that is with very LONG and should not overflow
              and push other elements out of the bounding box.
              <small>Feb 23, 2015 12:32 am</small>
            </span>
          }
          actions={<div />}
          description={
            <span>
              The following snippet of text is rendered as{' '}
              <a href="">link text</a>.
            </span>
          }
          stacked={false}
        />
        <ListView.Item
          key="item3"
          checkboxInput={<input type="checkbox" />}
          heading="Stacked Additional Info items"
          description={
            <span>
              The following snippet of text is rendered as{' '}
              <a href="">link text</a>.
            </span>
          }
          additionalInfo={[
            <ListView.InfoItem key="1" stacked>
              <strong>113,735</strong>
              <span>Service One</span>
            </ListView.InfoItem>,
            <ListView.InfoItem key="2" stacked>
              <strong>35%</strong>
              <span>Service Two</span>
            </ListView.InfoItem>,
          ]}
          stacked={false}
        />
        <ListView.Item
          key="item4"
          additionalInfo={[
            <ListView.InfoItem key="1">Only Additional</ListView.InfoItem>,
            <ListView.InfoItem key="2">Info Items</ListView.InfoItem>,
          ]}
          stacked={true}
        />
      </ListView>
    );
  }
}
