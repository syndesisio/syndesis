import { Button } from 'patternfly-react';
import * as React from 'react';
import { Dialog } from '../../Shared';
import { ITagIntegrationEntry } from './CiCdUIModels';

export interface ITagIntegrationDialogChildrenProps {
  handleChange: (
    items: ITagIntegrationEntry[],
    initialItems: ITagIntegrationEntry[]
  ) => void;
}

export interface ITagIntegrationDialogProps {
  i18nTitle: string;
  i18nCancelButtonText: string;
  i18nSaveButtonText: string;
  onHide: () => void;
  onSave: (items: ITagIntegrationEntry[]) => void;
  children: (props: ITagIntegrationDialogChildrenProps) => any;
}
export interface ITagIntegrationDialogState {
  disableSave: boolean;
}

export class TagIntegrationDialog extends React.Component<
  ITagIntegrationDialogProps,
  ITagIntegrationDialogState
> {
  private itemsDraft: ITagIntegrationEntry[] | undefined;
  constructor(props: ITagIntegrationDialogProps) {
    super(props);
    this.state = {
      disableSave: true,
    };
    this.handleClick = this.handleClick.bind(this);
    this.handleChange = this.handleChange.bind(this);
  }
  public handleChange(
    items: ITagIntegrationEntry[],
    initialItems: ITagIntegrationEntry[]
  ) {
    const disableSave = initialItems
      .map(
        (item, index) =>
          item.name === items[index].name &&
          item.selected === items[index].selected
      )
      .reduce((acc, current) => acc && current, true);
    this.itemsDraft = items;
    this.setState({ disableSave });
  }
  public handleClick() {
    this.props.onSave(this.itemsDraft!);
  }
  public render() {
    return (
      <Dialog
        body={this.props.children({
          handleChange: this.handleChange,
        })}
        footer={
          <>
            <Button
              data-testid={'tag-integration-dialog-cancel-button'}
              onClick={this.props.onHide}
            >
              {this.props.i18nCancelButtonText}
            </Button>
            <Button
              data-testid={'tag-integration-dialog-save-button'}
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
