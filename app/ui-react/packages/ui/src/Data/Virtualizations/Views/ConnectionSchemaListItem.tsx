import { ListView, ListViewIcon, ListViewItem } from 'patternfly-react';
import * as React from 'react';
import { toValidHtmlId } from '../../../helpers';

export interface IConnectionSchemaListItemProps {
  icon?: string;
  connectionName: string;
  connectionDescription: string;
}

export const ConnectionSchemaListItem: React.FunctionComponent<
  IConnectionSchemaListItemProps
> = props => {

  return (
    <>
      <ListViewItem
        data-testid={`connection-schema-list-item-${toValidHtmlId(
          props.connectionName
        )}-list-item`}
        heading={props.connectionName}
        description={
          props.connectionDescription
            ? props.connectionDescription
            : ''
        }
        hideCloseIcon={true}
        leftContent={
          props.icon ? (
            <div className="blank-slate-pf-icon">
              <img
                src={props.icon}
                alt={props.connectionName}
                width={46}
              />
            </div>
          ) : (
              <ListViewIcon name={'database'} />
            )
        }
        stacked={false}
      >
        {props.children ? (
          <ListView>{props.children}</ListView>
        ) : null}
      </ListViewItem>
    </>
  );

}
