import {
  Card,
  CardBody,
  CardHeader,
  Grid,
  GridItem,
  Title,
} from '@patternfly/react-core';
import { ErrorCircleOIcon } from '@patternfly/react-icons';
import { global_danger_color_100 } from '@patternfly/react-tokens';
import * as React from 'react';
import { UptimeMetric } from '../../Dashboard';
import { PageSection } from '../../Layout';
import { AggregatedMetricCard } from '../../Shared';
import './IntegrationDetailMetrics.css';

export interface IIntegrationDetailMetricsProps {
  i18nLastProcessed: string;
  i18nNoDataAvailable: string;
  i18nSince: string;
  i18nTotalErrors: string;
  i18nTotalMessages: string;
  i18nUptime: string;
  errors?: number;
  lastProcessed?: string;
  messages?: number;
  start?: number;
  uptimeDuration?: string;
}

export const IntegrationDetailMetrics: React.FunctionComponent<IIntegrationDetailMetricsProps> =
  ({
    i18nLastProcessed,
    i18nNoDataAvailable,
    i18nSince,
    i18nTotalErrors,
    i18nTotalMessages,
    i18nUptime,
    errors = 0,
    lastProcessed,
    messages = 0,
    start,
    uptimeDuration,
  }) => {
    const okMessagesCount = messages - errors;
    return (
      <PageSection className="integration-detail-metrics">
        <Grid md={6} xl={3} hasGutter={true}>
          <GridItem>
            <Card data-testid={'integration-detail-metrics-total-errors-card'}>
              <CardHeader>
                <Title size="lg" headingLevel={'h3'}>
                  {i18nTotalErrors}
                </Title>
              </CardHeader>
              <CardBody>
                <br />
                <Title
                  size={'xl'}
                  data-testid={'integration-detail-metrics-total-errors'}
                  headingLevel={'h4'}
                >
                  <ErrorCircleOIcon color={global_danger_color_100.value} />
                  &nbsp;{errors}
                </Title>
              </CardBody>
            </Card>
          </GridItem>
          <GridItem>
            <Card
              data-testid={'integration-detail-metrics-last-processed-card'}
            >
              <CardHeader>
                <Title size={'lg'} headingLevel={'h3'}>
                  {i18nLastProcessed}
                </Title>
              </CardHeader>
              <CardBody>
                <br />
                <Title
                  size={'xl'}
                  className="integration-detail-metrics__last-processed"
                  data-testid={'integration-detail-metrics-last-processed'}
                  headingLevel={'h4'}
                >
                  {lastProcessed ? lastProcessed : i18nNoDataAvailable}
                </Title>
              </CardBody>
            </Card>
          </GridItem>
          <GridItem>
            <AggregatedMetricCard
              data-testid={'integration-detail-metrics-total-messages-card'}
              title={i18nTotalMessages}
              ok={okMessagesCount}
              error={errors}
              total={messages}
            />
          </GridItem>
          <GridItem>
            <UptimeMetric
              start={start || 0}
              uptimeDuration={uptimeDuration || i18nNoDataAvailable}
              i18nSince={i18nSince}
              i18nTitle={i18nUptime}
            />
          </GridItem>
        </Grid>
      </PageSection>
    );
  };
