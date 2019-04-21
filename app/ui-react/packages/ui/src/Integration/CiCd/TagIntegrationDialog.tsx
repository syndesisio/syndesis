import { Button, ListView } from 'patternfly-react';
import * as React from 'react';
import { Dialog } from '../../Shared';
import { ITagIntegrationEntry } from './CiCdUIModels';
import { TagIntegrationListItem } from './TagIntegrationListItem';

export interface ITagIntegrationDialogProps {
  i18nTitle: string;
  i18nCancelButtonText: string;
  i18nSaveButtonText: string;
  i18nTagIntegrationDialogMessage: string;
  onHide: () => void;
  onSave: (items: ITagIntegrationEntry[]) => void;
  items: ITagIntegrationEntry[];
}
export interface ITagIntegrationDialogState {
  items: ITagIntegrationEntry[];
  disableSave: boolean;
}

export class TagIntegrationDialog extends React.Component<
  ITagIntegrationDialogProps,
  ITagIntegrationDialogState
> {
  constructor(props: ITagIntegrationDialogProps) {
    super(props);
    this.state = {
      disableSave: true,
      items: this.props.items,
    };
    this.handleClick = this.handleClick.bind(this);
    this.handleChange = this.handleChange.bind(this);
  }
  public handleChange(name: string, selected: boolean) {
    const items = this.state.items.map(item =>
      item.name === name ? { name, selected } : item
    );
    const needsSave = this.props.items
      .map(
        (item, index) =>
          item.name === items[index].name &&
          item.selected === items[index].selected
      )
      .reduce((acc, current) => acc && current, true);
    this.setState({ items, disableSave: needsSave });
  }
  public handleClick() {
    this.props.onSave(this.state.items);
  }
  public render() {
    return (
      <Dialog
        body={
          <>
            <p>{this.props.i18nTagIntegrationDialogMessage}</p>
            <ListView>
              {this.state.items.map((item, index) => (
                <TagIntegrationListItem
                  key={index}
                  name={item.name}
                  selected={item.selected}
                  onChange={this.handleChange}
                />
              ))}
            </ListView>
          </>
        }
        footer={
          <>
            <Button onClick={this.props.onHide}>
              {this.props.i18nCancelButtonText}
            </Button>
            <Button
              bsStyle={'primary'}
              onClick={this.handleClick}
              disabled={this.state.disableSave}
            >
              {this.props.i18nSaveButtonText}
            </Button>
          </>
        }
        title={this.props.i18nTitle}
        onHide={this.props.onHide}
      />
    );
  }
}
