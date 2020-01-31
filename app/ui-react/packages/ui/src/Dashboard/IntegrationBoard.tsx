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

export class IntegrationBoard extends React.PureComponent<
  IIntegrationBoardProps
> {
  public render() {
    const data = [
      {
        x: this.props.i18nIntegrationStateRunning,
        y: this.props.runningIntegrations,
      },
      {
        x: this.props.i18nIntegrationStateStopped,
        y: this.props.stoppedIntegrations,
      },
      {
        x: this.props.i18nIntegrationStatePending,
        y: this.props.pendingIntegrations,
      },
    ];
    const legendData = [
      {
        name: `${this.props.i18nIntegrationStateRunning}: ${this.props.runningIntegrations}`,
      },
      {
        name: `${this.props.i18nIntegrationStateStopped}: ${this.props.stoppedIntegrations}`,
      },
      {
        name: `${this.props.i18nIntegrationStatePending}: ${this.props.pendingIntegrations}`,
      },
    ];
    const total = `${this.props.runningIntegrations +
      this.props.stoppedIntegrations +
      this.props.pendingIntegrations}`;

    return (
      <Card data-testid={'dashboard-integration-board'}>
        <CardHeader>
          <Title size={'md'}>{this.props.i18nTitle}</Title>
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
                  subTitle={this.props.i18nIntegrations}
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
  }
}
