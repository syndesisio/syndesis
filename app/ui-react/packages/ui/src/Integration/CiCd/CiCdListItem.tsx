import { 
  Button, 
  ButtonVariant, 
  DataListAction, 
  DataListCell, 
  DataListItem, 
  DataListItemCells, 
  DataListItemRow, 
} from '@patternfly/react-core';
import * as React from 'react';
import { toValidHtmlId } from '../../helpers';

export interface ICiCdListItemProps {
  onEditClicked: (name: string) => void;
  onRemoveClicked: (name: string) => void;
  i18nEditButtonText: string;
  i18nRemoveButtonText: string;
  /**
   * Text string for the number of integrations using this tag
   */
  i18nUsesText: string;
  /**
   * Environment name
   */
  name: string;
}

export const CiCdListItem: React.FC<ICiCdListItemProps> = ({
  onEditClicked,
  onRemoveClicked,
  i18nEditButtonText,
  i18nRemoveButtonText,
  i18nUsesText,
  name,
}) => {
  const handleEditClicked = () => {
    onEditClicked(name);
  };
  const handleRemoveClicked = () => {
    onRemoveClicked(name);
  };
  return (
    <DataListItem
      aria-labelledby={'cicd list item'}
      data-testid={`cicd-list-item-${toValidHtmlId(name)}-list-item`}
    >
      <DataListItemRow>
        <DataListItemCells
          dataListCells={[
            <DataListCell key={'primary content'} width={2}>
              <div className={'cicd-list-item__text-wrapper'} data-testid={`cicd-list-item-name`}>
                <b>{name}</b>
              </div>
            </DataListCell>,
            <DataListCell key={'secondary content'} width={4}>
              <div className={'cicd-list-item__uses-text'} data-testid={`cicd-list-item-usage`}>
                {i18nUsesText}
              </div>
            </DataListCell>,
          ]}
        />
        <DataListAction
          aria-labelledby={'cicd list item action'}
          id={'cicd-list-item-action'}
          aria-label={'cicd-actions'}
        >
          <Button
            data-testid={'cicd-list-item-create-button'}
            variant={ButtonVariant.secondary}
            onClick={handleEditClicked}
          >
            {i18nEditButtonText}
          </Button>
          <Button
            data-testid={'cicd-list-item-remove-button'}
            variant={ButtonVariant.secondary}
            onClick={handleRemoveClicked}
          >
            {i18nRemoveButtonText}
          </Button>
        </DataListAction>
      </DataListItemRow>
    </DataListItem>
  );
};
