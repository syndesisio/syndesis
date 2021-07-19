import { ChartDonut } from '@patternfly/react-charts';
import {
  Card,
  CardBody,
  CardHeader,
  Grid,
  GridItem,
  Title,
} from '@patternfly/react-core';
import * as React from 'react';

export interface IIntegrationBoardProps {
  pendingIntegrations: number;
  runningIntegrations: number;
  stoppedIntegrations: number;
  i18nIntegrationStatePending: string;
  i18nIntegrationStateRunning: string;
  i18nIntegrationStateStopped: string;
  i18nIntegrations: string;
  i18nTitle: string;
}

export const IntegrationBoard: React.FunctionComponent<IIntegrationBoardProps> =
  ({
    pendingIntegrations,
    runningIntegrations,
    stoppedIntegrations,
    i18nIntegrationStatePending,
    i18nIntegrationStateRunning,
    i18nIntegrationStateStopped,
    i18nIntegrations,
    i18nTitle,
  }) => {
    const data = [
      {
        x: i18nIntegrationStateRunning,
        y: runningIntegrations,
      },
      {
        x: i18nIntegrationStateStopped,
        y: stoppedIntegrations,
      },
      {
        x: i18nIntegrationStatePending,
        y: pendingIntegrations,
      },
    ];
    const legendData = [
      {
        name: `${i18nIntegrationStateRunning}: ${runningIntegrations}`,
      },
      {
        name: `${i18nIntegrationStateStopped}: ${stoppedIntegrations}`,
      },
      {
        name: `${i18nIntegrationStatePending}: ${pendingIntegrations}`,
      },
    ];
    const total = `${
      runningIntegrations + stoppedIntegrations + pendingIntegrations
    }`;

    return (
      <Card data-testid={'dashboard-integration-board'}>
        <CardHeader>
          <Title size={'md'} headingLevel={'h3'}>
            {i18nTitle}
          </Title>
        </CardHeader>
        <CardBody>
          <Grid>
            <GridItem span={6} offset={3}>
              <div
                style={{
                  // these values are tied to how the chart figures out it's
                  // sizing, best to leave them here
                  height: '150px',
                  width: '275px',
                }}
              >
                <ChartDonut
                  data-test-id="integration-board"
                  constrainToVisibleArea={true}
                  data={data}
                  subTitle={i18nIntegrations}
                  title={total}
                  labels={({ datum }) => `${datum.x}: ${datum.y}`}
                  legendData={legendData}
                  legendOrientation="vertical"
                  legendPosition="right"
                  padding={{
                    bottom: 20,
                    left: 20,
                    right: 145, // Adjusted to accommodate legend
                    top: 20,
                  }}
                  height={150}
                  width={275}
                />
              </div>
            </GridItem>
          </Grid>
        </CardBody>
      </Card>
    );
  };
