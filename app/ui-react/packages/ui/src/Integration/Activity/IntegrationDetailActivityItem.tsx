import {
  //Badge,
  DataListCell,
  DataListContent,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  DataListToggle,
  //Flex,
  //FlexItem,
} from '@patternfly/react-core';
import { ErrorCircleOIcon, OkIcon } from '@patternfly/react-icons';
//import { ListView, Table } from 'patternfly-react';
import * as React from 'react';

import './IntegrationDetailActivityItem.css';
import { useState } from "react";

export interface IIntegrationDetailActivityItemProps {
  date: string;
  errorCount: number;
  i18nErrorsFound: string;
  i18nHeaderStep?: string;
  i18nHeaderTime?: string;
  i18nHeaderDuration?: string;
  i18nHeaderDurationUnit?: string;
  i18nHeaderStatus?: string;
  i18nHeaderOutput?: string;
  i18nNoErrors: string;
  i18nNoOutput?: string;
  i18nNoSteps: string;
  i18nVersion: string;
  steps: JSX.Element[];
  time: string;
  version?: string;
}
/*

const headerFormat = (value: string) => <Table.Heading>{value}</Table.Heading>;
const cellFormat = (value: string) => <Table.Cell>{value}</Table.Cell>;
const statusCellFormat = (status: string) => (
  <Table.Cell className="integration-detail-activity-item__status">
    {status === 'Success' ? (
      <>
        <OkIcon /> Success
      </>
    ) : (
      <>
        <ErrorCircleOIcon /> Error
      </>
    )}
  </Table.Cell>
);
const outputCellFormat = (output: string) => (
  <Table.Cell className="integration-detail-activity-item__output">
    <pre className="integration-detail-activity-item__output-step-data">
      {output || 'No output'}
    </pre>
  </Table.Cell>
);
*/

export const IntegrationDetailActivityItem: React.FC<
  IIntegrationDetailActivityItemProps
> = ( props ) => {
  //const toggleExpanded = (id: string) => {};

  // initial visibility of delete dialog
  const [rowExpanded, setRowExpanded] = useState(false);

  const doExpand = () => {
    setRowExpanded(!rowExpanded);
  };

  return (
    <DataListItem aria-labelledby="activity item" isExpanded={rowExpanded} className={'integration-detail-activity-item'}>
      <DataListItemRow>
        <DataListToggle
          onClick={doExpand}
          isExpanded={rowExpanded}
          id="activity-toggle"
        />
        <DataListItemCells
          dataListCells={[
            <DataListCell id="activity-date" key="date">
              {props.date}
            </DataListCell>,
            <DataListCell id="activity-time" key="time">
              {props.time}
            </DataListCell>,
            <DataListCell id="activity-version" key="version">
              {props.i18nVersion}
              &nbsp;
              {props.version}
            </DataListCell>,
            <DataListCell id="activity-errors" key="errors">
              <div className={'integration-detail-activity-item__status-item'}>
                {props.errorCount > 0 ? (
                  <>
                    <ErrorCircleOIcon />
                    {'  '}
                    {props.i18nErrorsFound}
                  </>
                ) : (
                  <>
                    <OkIcon />
                    {'  '}
                    {props.i18nNoErrors}
                  </>
                )}
              </div>
              {/*<Flex>
                <FlexItem>
                  <span id="time">Errors</span>
                </FlexItem>
                <FlexItem>
                  <Badge isRead>2</Badge>
                </FlexItem>
              </Flex>*/}
            </DataListCell>,
          ]}
        />
      </DataListItemRow>
      <DataListContent
        aria-label="Primary Content Details"
        id="ex-expand1"
        isHidden={!rowExpanded}
      >
        <p>
          Something goes here.
        </p>
      </DataListContent>
    </DataListItem>
    /*<ListViewItem
      className={'integration-detail-activity-item'}
      key={props.time}
      heading={props.date}
      description={props.time}
      additionalInfo={[
        <ListView.InfoItem key={1}>
          {props.i18nVersion}
          &nbsp;
          {props.version}
        </ListView.InfoItem>,
        <ListView.InfoItem key={2}>
          <div className={'integration-detail-activity-item__status-item'}>
            {props.errorCount > 0 ? (
              <>
                <ErrorCircleOIcon />
                {'  '}
                {props.i18nErrorsFound}
              </>
            ) : (
              <>
                <OkIcon />
                {'  '}
                {props.i18nNoErrors}
              </>
            )}
          </div>
        </ListView.InfoItem>,
      ]}
    >
      {props.steps ? (
        <Table.PfProvider
          className={'integration-detail-activity-item__expanded-table'}
          striped={true}
          bordered={true}
          hover={true}
          columns={[
            {
              cell: {
                formatters: [cellFormat],
                property: 'step',
              },
              header: {
                formatters: [headerFormat],
                label: 'Step',
              },
            },
            {
              cell: {
                formatters: [cellFormat],
                property: 'time',
              },
              header: {
                formatters: [headerFormat],
                label: 'Time',
              },
            },
            {
              cell: {
                formatters: [cellFormat],
                property: 'duration',
              },
              header: {
                formatters: [headerFormat],
                label: 'Duration',
              },
            },
            {
              cell: {
                formatters: [statusCellFormat],
                property: 'status',
              },
              header: {
                formatters: [headerFormat],
                label: 'Status',
              },
            },
            {
              cell: {
                formatters: [outputCellFormat],
                property: 'output',
              },
              header: {
                formatters: [headerFormat],
                label: 'Output',
              },
            },
          ]}
        >
          <Table.Header />
          {props.steps}
        </Table.PfProvider>
      ) : (
        <span>{props.i18nNoSteps}</span>
      )}
    </ListViewItem>*/
  );
};
