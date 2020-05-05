import { Button, ButtonVariant } from '@patternfly/react-core';
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

export const TagIntegrationDialog: React.FunctionComponent<ITagIntegrationDialogProps> = props => {

  const [disableSave, setDisableSave] = React.useState(true);
  const [draftItems, setDraftItems] = React.useState<ITagIntegrationEntry[]>([]);

  const handleSelectionChange = (
    items: ITagIntegrationEntry[],
    initialItems: ITagIntegrationEntry[]
  ) => {
    setDraftItems(items);
    const shouldDisable = initialItems
      .map(
        (item, index) =>
          item.name === items[index].name &&
          item.selected === items[index].selected
      )
      .reduce((acc, current) => acc && current, true);
    setDisableSave(shouldDisable);
  }

  const handleClick = () => {
    props.onSave(draftItems);
  }

  return (
      <Dialog
        body={props.children({
          handleChange: handleSelectionChange,
        })}
        footer={
          <>
            <Button
              data-testid={'tag-integration-dialog-cancel-button'}
              onClick={props.onHide}
            >
              {props.i18nCancelButtonText}
            </Button>
            <Button
              data-testid={'tag-integration-dialog-save-button'}
              variant={ButtonVariant.primary}
              onClick={handleClick}
              isDisabled={disableSave}
            >
              {props.i18nSaveButtonText}
            </Button>
          </>
        }
        title={props.i18nTitle}
        onHide={props.onHide}
      />
    );
}
