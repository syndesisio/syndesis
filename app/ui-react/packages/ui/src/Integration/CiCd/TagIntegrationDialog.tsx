import { Button } from 'patternfly-react';
import * as React from 'react';
import { Dialog } from '../../Shared';
import { ITagIntegrationEntry } from './CiCdUIModels';

export interface ITagIntegrationDialogChildrenProps {
  handleChange: (name: string, selected: boolean) => void;
  items: ITagIntegrationEntry[];
}

export interface ITagIntegrationDialogProps {
  i18nTitle: string;
  i18nCancelButtonText: string;
  i18nSaveButtonText: string;
  onHide: () => void;
  onSave: (items: ITagIntegrationEntry[]) => void;
  initialItems: ITagIntegrationEntry[];
  children: (props: ITagIntegrationDialogChildrenProps) => any;
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
      items: this.props.initialItems,
    };
    this.handleClick = this.handleClick.bind(this);
    this.handleChange = this.handleChange.bind(this);
  }
  public handleChange(name: string, selected: boolean) {
    const items = this.props.initialItems.map(item =>
      item.name === name ? { name, selected } : item
    );
    const needsSave = this.props.initialItems
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
        body={this.props.children({
          handleChange: this.handleChange,
          items: this.state.items,
        })}
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
