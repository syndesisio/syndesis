import {
  Bullseye,
  EmptyState,
  EmptyStateBody,
  EmptyStateVariant,
  Title,
} from '@patternfly/react-core';
import { ErrorCircleOIcon, OkIcon } from '@patternfly/react-icons';
import { classNames, Table, TableBody, TableHeader } from '@patternfly/react-table';
import { Spinner } from 'patternfly-react';
import * as React from 'react';

export interface IVirtualizationHistoryItem {
  actions: JSX.Element;
  publishedState: 'RUNNING' | 'NOTFOUND' | 'IN_PROGRESS' | 'FAILED';
  timePublished: string;
  version: number;
}

export interface IVirtualizationDetailHistoryTableProps {
  /**
   * Accessibility message for the table column for the kebab menu.
   */
  a11yActionMenuColumn: string;
  historyItems: IVirtualizationHistoryItem[];
  i18nEmptyVersionsTitle: string;
  i18nEmptyVersionsMsg: string;
  isModified: boolean;
  /**
   * i18n column headers in this order: version, published time, published indicator, kebab menu
   */
  tableHeaders: string[];
}

const getPublishIcon = (publishState: string) => {
  if (publishState === 'RUNNING') {
    return <OkIcon key="icon" color="green" />;
  } else if (publishState === 'FAILED') {
    return <ErrorCircleOIcon key="icon" color="red" />;
  } else if (publishState === 'IN_PROGRESS') {
    return <Spinner loading={true} inline={true} />;
  }
  return null;
};

const getColumns = (headers: string[]) => {
  const cols = [
    {
      columnTransforms: [classNames('pf-m-fit-content')], // column sized to heading and data
      title: headers[0],
    },
    {
      columnTransforms: [classNames('pf-m-fit-content')],
      title: headers[1],
    },
    {
      columnTransforms: [classNames('pf-m-width-max')], // column sized to take up remaining space
      title: headers[2],
    },
    {
      columnTransforms: [classNames('pf-m-fit-content')],
      title: headers[3],
    },
  ];
  return cols;
};

const getRows = (
  items: IVirtualizationHistoryItem[],
  emptyVersionsTitle: string,
  emptyVersionsMsg: string
) => {
  const rows: any[] = [];
  if (items.length === 0) {
    const row = {
      cells: [
        {
          props: { colSpan: 8 },
          title: (
            <Bullseye>
              <EmptyState variant={EmptyStateVariant.small}>
                <Title headingLevel="h2" size="lg">
                  {emptyVersionsTitle}
                </Title>
                <EmptyStateBody>{emptyVersionsMsg}</EmptyStateBody>
              </EmptyState>
            </Bullseye>
          ),
        },
      ],
      heightAuto: true,
    };
    rows.push(row);
  } else {
    for (const item of items) {
      const row = {
        cells: [
          {
            title: item.version,
          },
          {
            title: item.timePublished,
          },
          {
            title: getPublishIcon(item.publishedState),
          },
          {
            title: item.actions,
          },
        ],
      };
      rows.push(row);
    }
  }
  return rows;
};

export const VirtualizationDetailHistoryTable: React.FunctionComponent<
  IVirtualizationDetailHistoryTableProps
> = props => {
  return (
    <Table
      aria-label={props.a11yActionMenuColumn}
      cells={getColumns(props.tableHeaders)}
      rows={getRows(
        props.historyItems,
        props.i18nEmptyVersionsTitle,
        props.i18nEmptyVersionsMsg
      )}
    >
      <TableHeader />
      <TableBody />
    </Table>
  );
};
