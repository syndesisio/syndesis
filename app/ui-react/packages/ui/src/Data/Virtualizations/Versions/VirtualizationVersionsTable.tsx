import {
  Bullseye,
  Card,
  CardBody,
  EmptyState,
  EmptyStateBody,
  EmptyStateVariant,
  Spinner,
  Split,
  SplitItem,
  Stack,
  StackItem,
  Text,
  TextContent,
  TextVariants,
  Title,
} from '@patternfly/react-core';
import { ErrorCircleOIcon, OkIcon } from '@patternfly/react-icons';
import {
  classNames,
  Table,
  TableBody,
  TableHeader,
} from '@patternfly/react-table';
import * as React from 'react';
import './VirtualizationVersionsTable.css';

export interface IVirtualizationVersionItem {
  actions: JSX.Element;
  publishedState: 'RUNNING' | 'NOTFOUND' | 'IN_PROGRESS' | 'FAILED';
  timePublished: string;
  version: number;
}

export interface IVirtualizationVersionsTableProps {
  /**
   * Accessibility message for the table column for the kebab menu.
   */
  a11yActionMenuColumn: string;
  draftActions: JSX.Element;
  versionItems: IVirtualizationVersionItem[];
  i18nDraft: string;
  i18nEmptyVersionsTitle: string;
  i18nEmptyVersionsMsg: string;
  i18nPublish: string;
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
    return <Spinner size={'lg'} />;
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

const getRows = (items: IVirtualizationVersionItem[]) => {
  const rows: any[] = [];
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
  return rows;
};

export const VirtualizationVersionsTable: React.FunctionComponent<
  IVirtualizationVersionsTableProps
> = props => {
  return (
    <Stack>
      {props.isModified && (
        <StackItem>
          <Card className={'virtualization-versions-table__draft'}>
            <CardBody className={'virtualization-versions-table__draft-body'}>
              <Split>
                <SplitItem>
                  <Title size="lg">{props.i18nDraft}</Title>
                </SplitItem>
                <SplitItem isFilled={true} />
                <SplitItem>{props.draftActions}</SplitItem>
              </Split>
            </CardBody>
          </Card>
        </StackItem>
      )}
      <StackItem>
        {props.versionItems.length > 0 ? (
          <Table
            aria-label={props.a11yActionMenuColumn}
            cells={getColumns(props.tableHeaders)}
            rows={getRows(props.versionItems)}
          >
            <TableHeader />
            <TableBody />
          </Table>
        ) : (
          <Bullseye>
            <EmptyState variant={EmptyStateVariant.small}>
              <Title headingLevel="h2" size="lg">
                {props.i18nEmptyVersionsTitle}
              </Title>
              <EmptyStateBody>
                <TextContent>
                  <Text component={TextVariants.small}>
                    {props.i18nEmptyVersionsMsg}
                  </Text>
                </TextContent>
              </EmptyStateBody>
            </EmptyState>
          </Bullseye>
        )}
      </StackItem>
    </Stack>
  );
};
