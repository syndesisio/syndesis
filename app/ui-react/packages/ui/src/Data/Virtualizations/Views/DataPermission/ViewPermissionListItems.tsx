import {
  Badge,
  Button,
  DataListCell,
  DataListCheck,
  DataListContent,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  DataListToggle,
} from '@patternfly/react-core';
import * as React from 'react';
import './ViewPermissionListItems.css';

export interface IViewPermissionListItemsProps {
  viewId: string;
  viewName: string;
  itemSelected: string[];
  onSelectedViewChange: (checked: boolean, event: any, view: string) => void;
}

export const ViewPermissionListItems: React.FC<IViewPermissionListItemsProps> = props => {
  /**
   * React useState Hook to handle state in component.
   */
  const [show, setShow] = React.useState<boolean>(false);

  const [permissionList] = React.useState<string[]>([
    'Developer : Read / Edit / Delete',
    'Admin : Read / Edit / Delete',
    'User : Read / Edit / Delete',
    'UX : Read / Edit / Delete',
    'New User : Read / Edit / Delete',
    'Consumer : Read / Edit / Delete',
    'Developer : Read / Edit / Delete',
  ]);
  const [trimPermissionList, setTrimPermissionList] = React.useState<string[]>(
    []
  );

  const [showAll, setShowAll] = React.useState<boolean>(false);

  React.useEffect(() => {
    if (permissionList.length > 4) {
      const copyList = permissionList.slice();
      copyList.length = 4;
      setTrimPermissionList(copyList);
    }
  });

  return (
    <DataListItem aria-labelledby="width-ex3-item1" isExpanded={show}>
      <DataListItemRow>
        <DataListToggle
          isExpanded={show}
          id="width-ex3-toggle1"
          aria-controls="width-ex3-expand1"
          // tslint:disable-next-line: jsx-no-lambda
          onClick={() => setShow(!show)}
        />
        <DataListCheck
          aria-labelledby="width-ex3-item1"
          name="width-ex3-item1"
          checked={props.itemSelected.includes(props.viewName)}
          // tslint:disable-next-line: jsx-no-lambda
          onChange={(checked: boolean, event: any) =>
            props.onSelectedViewChange(checked, event, props.viewName)
          }
        />
        <DataListItemCells
          dataListCells={[
            <DataListCell width={1} key={props.viewId}>
              <span id="check-action-item2">{props.viewName}</span>
            </DataListCell>,
            <DataListCell width={5} key={`temp-${props.viewId}`}>
              {permissionList.length > 4 && !showAll
                ? trimPermissionList.map((permissionSet, index) => (
                    <Badge
                      key={`temp2-${index}`}
                      isRead={true}
                      className={'view-permission-list-items-permission_badge'}
                    >
                      {permissionSet}
                    </Badge>
                  ))
                : permissionList.map((permissionSet, index) => (
                    <Badge
                      key={`temp2-${index}`}
                      isRead={true}
                      className={'view-permission-list-items-permission_badge'}
                    >
                      {permissionSet}
                    </Badge>
                  ))}
              {trimPermissionList.length > 0 && (
                // tslint:disable-next-line: jsx-no-lambda
                <Button variant={'link'} onClick={() => setShowAll(!showAll)}>
                  {showAll
                    ? 'Show less'
                    : `${permissionList.length - 4}more...`}{' '}
                </Button>
              )}
            </DataListCell>,
          ]}
        />
      </DataListItemRow>
      <DataListContent
        aria-label="Primary Content Details"
        id="width-ex3-expand1"
        isHidden={!show}
      >
        <p>
          Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do
          eiusmod tempor incididunt ut labore et dolore magna aliqua.
        </p>
      </DataListContent>
    </DataListItem>
  );
};
