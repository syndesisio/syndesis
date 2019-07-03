// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import { Label, ListViewInfoItem, ListViewItem } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../../helpers';

import './ViewInfoListItem.css';

export interface IViewInfoListItemProps {
  name: string;
  description?: string;
  connectionName: string;
  nodePath: string[];
  selected: boolean;
  i18nUpdate: string;
  isUpdateView: boolean;
  onSelectionChanged: (name: string, selected: boolean) => void;
}

export interface IViewInfoListItemState {
  itemSelected: boolean;
}

export const ViewInfoListItem: React.FunctionComponent<
  IViewInfoListItemProps
> = props => {

  const [itemSelected, setItemSelected] = React.useState(props.selected);

  const getNodePathStr = () => {
    let path = '';
    for (const segment of props.nodePath) {
      path += '/' + segment;
    }
    return path;
  }

  const doToggleCheckbox = (viewName: string) => (event: any) => {
    setItemSelected(!itemSelected);

    props.onSelectionChanged(viewName, !itemSelected);
  };

  return (
    <ListViewItem
      data-testid={`view-info-list-item-${toValidHtmlId(
        props.name
      )}-list-item`}
      className={'view-info-list-item'}
      heading={props.name}
      description={getNodePathStr()}
      checkboxInput={
        <input
          data-testid={'view-info-list-item-selected-input'}
          type="checkbox"
          value=""
          defaultChecked={props.selected}
          onChange={doToggleCheckbox(props.name)}
        />
      }
      additionalInfo={[
        <ListViewInfoItem key={1}>
          {props.isUpdateView === true ? (
            <Label type="warning">{props.i18nUpdate}</Label>
          ) : (
              ''
            )}
        </ListViewInfoItem>,
      ]}
      hideCloseIcon={true}
      stacked={false}
    />
  );
}
