import { 
  DataListCell, 
  DataListCheck, 
  DataListItem, 
  DataListItemCells, 
  DataListItemRow 
} from '@patternfly/react-core';
import * as React from 'react';
import { toValidHtmlId } from '../../helpers';
import './TagIntegrationListItem.css';

export interface ITagIntegrationListItemProps {
  selected: boolean;
  name: string;
  onChange: (name: string, selected: boolean) => void;
}

export const TagIntegrationListItem: React.FunctionComponent<ITagIntegrationListItemProps> = props => {

  const [itemSelected, setItemSelected] = React.useState(props.selected);

  return (
    <DataListItem
      aria-labelledby={'tag integration list item'}
      data-testid={`tag-integration-list-item-${toValidHtmlId(
        props.name
      )}-selected-input`}
      className={'tag-integration-list-item'}
    >
      <DataListItemRow>
        <DataListCheck
          aria-labelledby="tag-integration-list-item-check"
          name="tag-integration-list-item-check"
          data-testid={`tag-integration-list-item-check`}
          checked={itemSelected}
          // tslint:disable-next-line: jsx-no-lambda
          onChange={(checked, event) => {setItemSelected(checked); props.onChange(props.name, checked)}}
        />
        <DataListItemCells
          dataListCells={[
            <DataListCell key={'primary content'} width={2}>
              <div 
                className={'tag-integration-list-item__text-wrapper'}
                data-testid={`tag-integration-list-item-text`}
              >
                <b>{props.name}</b>
              </div>
            </DataListCell>,
          ]}
        />
      </DataListItemRow>
    </DataListItem>
  );
};
