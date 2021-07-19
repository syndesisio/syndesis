import {
  Bullseye,
  Card,
  CardBody,
  Grid,
  GridItem,
  Title,
} from '@patternfly/react-core';
import { ErrorCircleOIcon, OkIcon } from '@patternfly/react-icons';
import {
  global_danger_color_100,
  global_success_color_100,
} from '@patternfly/react-tokens';
import * as React from 'react';

export interface IAggregatedMetricProps {
  title: string;
  ok: number;
  error: number;
  total: number;
}

const formatNumber = (num: number) =>
  num.toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, '$1,');

export const AggregatedMetricCard: React.FunctionComponent<IAggregatedMetricProps> =
  ({ error, ok, title, total }) => (
    <Card className={'aggregate-status'}>
      <CardBody>
        <Bullseye>
          <Title size={'lg'} headingLevel={'h3'}>
            <span data-testid={'aggregated-metric-card-total-count'}>
              {formatNumber(total)}
            </span>
            <span data-testid={'aggregated-metric-card-title'}> {title}</span>
          </Title>
        </Bullseye>
      </CardBody>
      <CardBody>
        <Grid>
          <GridItem span={6}>
            <Bullseye>
              <Title size={'lg'} headingLevel={'h4'}>
                <OkIcon color={global_success_color_100.value} />
                &nbsp;
                <span data-testid={'aggregated-metric-card-ok-count'}>
                  {formatNumber(ok)}
                </span>
              </Title>
            </Bullseye>
          </GridItem>
          <GridItem span={6}>
            <Bullseye>
              <Title size={'lg'} headingLevel={'h4'}>
                <ErrorCircleOIcon color={global_danger_color_100.value} />
                &nbsp;
                <span data-testid={'aggregated-metric-card-error-count'}>
                  {formatNumber(error)}
                </span>
              </Title>
            </Bullseye>
          </GridItem>
        </Grid>
      </CardBody>
    </Card>
  );
