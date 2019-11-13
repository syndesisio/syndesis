import {
  Bullseye,
  EmptyState,
  EmptyStateBody,
  EmptyStateVariant,
  PageSection,
  Stack,
  StackItem,
  Title,
} from '@patternfly/react-core';
import { ErrorCircleOIcon, OkIcon } from '@patternfly/react-icons';
import { Table, TableBody, TableHeader } from '@patternfly/react-table';
import { Spinner } from 'patternfly-react';
import * as React from 'react';

export interface IVirtualizationHistoryItem {
  actions: JSX.Element;
  publishedState: 'RUNNING' | 'NOTFOUND' | 'IN_PROGRESS' | 'FAILED';
  timePublished: string;
  version: number;
}

export interface IVirtualizationDetailHistoryTableProps {
  historyItems: IVirtualizationHistoryItem[];
  i18nEmptyVersionsTitle: string;
  i18nEmptyVersionsMsg: string;
  isModified: boolean;
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
  return headers.map(header => ({
    title: header,
  }));
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
    <PageSection>
      <Stack>
        <StackItem>
          <Table
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
        </StackItem>
      </Stack>
    </PageSection>
  );
};
