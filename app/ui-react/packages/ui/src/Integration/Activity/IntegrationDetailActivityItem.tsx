import {
  DataListCell,
  DataListContent,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  DataListToggle,
} from '@patternfly/react-core';
import { ErrorCircleOIcon, OkIcon } from '@patternfly/react-icons';
import {
  Table,
  TableBody,
  TableHeader,
  TableVariant
} from '@patternfly/react-table';
import {
  global_danger_color_100,
  global_success_color_100,
} from '@patternfly/react-tokens';
import * as React from 'react';

import './IntegrationDetailActivityItem.css';
import { useState } from 'react';

export interface IIntegrationDetailActivityItemProps {
  date: string;
  errorCount: number;
  i18nError: string;
  i18nErrorsFound: string;
  i18nHeaderStep: string;
  i18nHeaderTime: string;
  i18nHeaderDuration: string;
  i18nHeaderDurationUnit?: string;
  i18nHeaderStatus: string;
  i18nHeaderOutput: string;
  i18nNoErrors: string;
  i18nNoOutput: string;
  i18nNoSteps: string;
  i18nSuccess: string;
  i18nVersion: string;
  steps: any;
  time: string;
  version?: string;
}

export const IntegrationDetailActivityItem: React.FC<
  IIntegrationDetailActivityItemProps
> = ( props ) => {
  const [rowExpanded, setRowExpanded] = useState(false);

  const doExpand = () => {
    setRowExpanded(!rowExpanded);
  };

  const rows = () => {
    const newRows = props.steps.map((step: any) => {
      return [{
        cells: [
          step.name,
          step.date,
          step.duration,
          {
            title: (
              step.status === 'Success' ? (
                <>
                  <OkIcon size={'sm'} color={global_success_color_100.value} />{'  '}{props.i18nSuccess}
                </>
              ) : (
                <>
                  <ErrorCircleOIcon size={'sm'} color={global_danger_color_100.value} />{'  '}{props.i18nError}
                </>
              )
            )
          },
          {
            title: (
              <pre className={'integration-detail-activity-item__output-step-data'}>
                {step.output || props.i18nNoOutput}
              </pre>
            )
          }
        ],
      }];
    });

    return newRows.reduce((a: any, b: any) => a.concat(b), []);
  };

  const columns = [
    props.i18nHeaderStep,
    props.i18nHeaderTime,
    props.i18nHeaderDuration,
    props.i18nHeaderStatus,
    props.i18nHeaderOutput
  ];

  return (
    <DataListItem aria-labelledby="activity item" isExpanded={rowExpanded} className={'integration-detail-activity-item'}>
      <DataListItemRow>
        <DataListToggle
          onClick={doExpand}
          isExpanded={rowExpanded}
          id="activity-item-toggle"
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
                    <ErrorCircleOIcon size={'sm'} color={global_danger_color_100.value} />
                    {'  '}
                    {props.i18nErrorsFound}
                  </>
                ) : (
                  <>
                    <OkIcon size={'sm'} color={global_success_color_100.value} />
                    {'  '}
                    {props.i18nNoErrors}
                  </>
                )}
              </div>
            </DataListCell>,
          ]}
        />
      </DataListItemRow>
      <DataListContent
        aria-label="Activity Item"
        id="activity-item"
        isHidden={!rowExpanded}
      >
        {props.steps ? (
          <Table
            aria-label={'integration detail activity item table'}
            cells={columns}
            rows={rows()}
            variant={TableVariant.compact}
          >
            <TableHeader />
            <TableBody />
          </Table>
        ) : (
          <span>{props.i18nNoSteps}</span>
        )}
      </DataListContent>
    </DataListItem>
  );
};
