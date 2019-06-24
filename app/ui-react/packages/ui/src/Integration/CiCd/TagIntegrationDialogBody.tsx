import * as H from '@syndesis/history';
import * as React from 'react';
import { CiCdList } from './CiCdList';
import { ITagIntegrationEntry } from './CiCdUIModels';
import { TagIntegrationDialogEmptyState } from './TagIntegrationDialogEmptyState';
import { TagIntegrationListItem } from './TagIntegrationListItem';

export interface ITagIntegrationDialogBodyProps {
  manageCiCdHref: H.LocationDescriptor;
  initialItems: ITagIntegrationEntry[];
  onChange: (
    items: ITagIntegrationEntry[],
    initialItems: ITagIntegrationEntry[]
  ) => void;
  i18nEmptyStateTitle: string;
  i18nEmptyStateInfo: string;
  i18nEmptyStateButtonText: string;
}

export interface ITagIntegrationDialogBodyState {
  items: ITagIntegrationEntry[];
}

export class TagIntegrationDialogBody extends React.Component<
  ITagIntegrationDialogBodyProps,
  ITagIntegrationDialogBodyState
> {
  constructor(props: ITagIntegrationDialogBodyProps) {
    super(props);
    this.state = {
      items: this.props.initialItems,
    };
    this.handleChange = this.handleChange.bind(this);
  }
  public handleChange(name: string, selected: boolean) {
    const items = this.state.items.map(item =>
      item.name === name ? { name, selected } : item
    );
    this.setState({ items });
    this.props.onChange(items, this.props.initialItems);
  }
  public render() {
    return (
      <>
        {this.state.items.length > 0 && (
          <>
            <CiCdList>
              {this.state.items.map((item, index) => (
                <TagIntegrationListItem
                  key={index}
                  name={item.name}
                  selected={item.selected}
                  onChange={this.handleChange}
                />
              ))}
            </CiCdList>
          </>
        )}
        {this.state.items.length === 0 && (
          <TagIntegrationDialogEmptyState
            href={this.props.manageCiCdHref}
            i18nTitle={this.props.i18nEmptyStateTitle}
            i18nInfo={this.props.i18nEmptyStateInfo}
            i18nGoToManageCiCdButtonText={this.props.i18nEmptyStateButtonText}
          />
        )}
      </>
    );
  }
}
