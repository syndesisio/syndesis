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
  cellWidth,
  Table,
  TableBody,
  TableHeader,
  TableVariant,
} from '@patternfly/react-table';
import {
  global_danger_color_100,
  global_success_color_100,
} from '@patternfly/react-tokens';
import * as React from 'react';
import { useState } from 'react';

import './IntegrationDetailActivityItem.css';

export interface IIntegrationDetailActivityItemProps {
  date: string;
  errorCount: number;
  i18nError: string;
  i18nErrorsFound: string;
  i18nHeaderStep: string;
  i18nHeaderTime: string;
  i18nHeaderDuration: string;
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

export const IntegrationDetailActivityItem: React.FC<IIntegrationDetailActivityItemProps> =
  ({
    date,
    errorCount,
    i18nError,
    i18nErrorsFound,
    i18nHeaderStep,
    i18nHeaderTime,
    i18nHeaderDuration,
    i18nHeaderStatus,
    i18nHeaderOutput,
    i18nNoErrors,
    i18nNoOutput,
    i18nNoSteps,
    i18nSuccess,
    i18nVersion,
    steps,
    time,
    version,
  }) => {
    const [rowExpanded, setRowExpanded] = useState(false);

    const doExpand = () => {
      setRowExpanded(!rowExpanded);
    };

    const rows = () => {
      const newRows = steps.map((step: any) => {
        return [
          {
            cells: [
              step.name,
              step.time,
              step.duration,
              {
                title:
                  step.status === 'Success' ? (
                    <>
                      <OkIcon
                        size={'sm'}
                        color={global_success_color_100.value}
                      />
                      {'  '}
                      {i18nSuccess}
                    </>
                  ) : (
                    <>
                      <ErrorCircleOIcon
                        size={'sm'}
                        color={global_danger_color_100.value}
                      />
                      {'  '}
                      {i18nError}
                    </>
                  ),
              },
              {
                title: (
                  <pre
                    className={
                      'integration-detail-activity-item__output-step-data'
                    }
                  >
                    {step.output || i18nNoOutput}
                  </pre>
                ),
              },
            ],
          },
        ];
      });

      return newRows.reduce((a: any, b: any) => a.concat(b), []);
    };

    const columns = [
      {
        columnTransforms: [cellWidth(10)],
        title: i18nHeaderStep,
      },
      {
        columnTransforms: [cellWidth(15)],
        title: i18nHeaderTime,
      },
      {
        columnTransforms: [cellWidth(10)],
        title: i18nHeaderDuration,
      },
      {
        columnTransforms: [cellWidth(10)],
        title: i18nHeaderStatus,
      },
      {
        columnTransforms: [
          () => ({
            className: 'integration-detail-activity-item__output-step-data',
          }),
          cellWidth(100),
        ],
        title: i18nHeaderOutput,
      },
    ];

    return (
      <DataListItem
        aria-labelledby="activity item"
        isExpanded={rowExpanded}
        className={'integration-detail-activity-item'}
        data-testid={'integration-detail-activity-item'}
      >
        <DataListItemRow>
          <DataListToggle
            onClick={doExpand}
            isExpanded={rowExpanded}
            id="activity-item-toggle"
            data-testid={'integration-detail-activity-item-toggle'}
          />
          <DataListItemCells
            dataListCells={[
              <DataListCell
                id="activity-date"
                data-testid={'integration-detail-activity-item-date'}
                key="date"
              >
                {date}
              </DataListCell>,
              <DataListCell
                id="activity-time"
                data-testid={'integration-detail-activity-item-time'}
                key="time"
              >
                {time}
              </DataListCell>,
              <DataListCell
                id="activity-version"
                data-testid={'integration-detail-activity-item-version'}
                key="version"
              >
                {i18nVersion}
                &nbsp;
                {version}
              </DataListCell>,
              <DataListCell
                id="activity-errors"
                data-testid={'integration-detail-activity-item-errors'}
                key="errors"
              >
                <div
                  className={'integration-detail-activity-item__status-item'}
                >
                  {errorCount > 0 ? (
                    <>
                      <ErrorCircleOIcon
                        size={'sm'}
                        color={global_danger_color_100.value}
                      />
                      {'  '}
                      {i18nErrorsFound}
                    </>
                  ) : (
                    <>
                      <OkIcon
                        size={'sm'}
                        color={global_success_color_100.value}
                      />
                      {'  '}
                      {i18nNoErrors}
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
          {steps ? (
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
            <span>{i18nNoSteps}</span>
          )}
        </DataListContent>
      </DataListItem>
    );
  };
