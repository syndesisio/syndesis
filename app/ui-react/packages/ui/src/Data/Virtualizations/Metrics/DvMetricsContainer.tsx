import {
  Bullseye,
  Card,
  EmptyState,
  EmptyStateBody,
  EmptyStateVariant,
  Flex,
  FlexItem,
  Text,
  TextContent,
  TextVariants,
  Title,
} from '@patternfly/react-core';
import * as React from 'react';
import {
  DvClientSessionMetric,
  IDvClientSessionMetricProps,
} from './DvClientSessionMetric';
import { DvRequestMetric, IDvRequestMetricProps } from './DvRequestMetric';
import { DvResultSetCacheMetric, IDvResultSetCacheMetricProps } from './DvResultSetCacheMetric';
import { DvUptimeMetric, IDvUptimeMetricProps } from './DvUptimeMetric';

/**
 * A component container for DV metrics.
 * @property {IDvClientSessionMetricProps} clientSessionProps - if set, the clien session metric component is shown
 * @property {string} i18nNoDataTitle - the localized title used when no metric components are used
 * @property {string} i18nNoDataDescription - the localized description used when no metric components are used
 * @property {IDvRequestMetricProps} requestProps - if set, the request metric component is shown
 * @property {IDvResultSetCacheMetricProps} resultSetCacheProps - if set, the result set cache metric is shown
 * @property {IDvUptimeMetricProps} uptimeProps - if set, the uptime metric component is shown
 */
export interface IDvMetricsContainer {
  clientSessionProps?: IDvClientSessionMetricProps;
  i18nNoDataTitle: string;
  i18nNoDataDescription: string;
  requestProps?: IDvRequestMetricProps;
  resultSetCacheProps?: IDvResultSetCacheMetricProps;
  uptimeProps?: IDvUptimeMetricProps;
}

export const DvMetricsContainer: React.FunctionComponent<
  IDvMetricsContainer
> = props => {
  if (
    props.clientSessionProps ||
    props.requestProps ||
    props.resultSetCacheProps ||
    props.uptimeProps
  ) {
    return (
      <Flex
        breakpointMods={[{ modifier: 'space-items-xl', breakpoint: 'xl' }]}
        className={'dv-metrics-container__flexAlign'}
      >
        {props.clientSessionProps && (
          <FlexItem breakpointMods={[{ modifier: 'flex-1', breakpoint: 'xl' }]}>
            <Card isHoverable={true} className="dv-metrics-container__card">
              <DvClientSessionMetric {...props.clientSessionProps} />
            </Card>
          </FlexItem>
        )}

        {props.requestProps && (
          <FlexItem breakpointMods={[{ modifier: 'flex-1', breakpoint: 'xl' }]}>
            <Card isHoverable={true} className="dv-metrics-container__card">
              <DvRequestMetric {...props.requestProps} />
            </Card>
          </FlexItem>
        )}

        {props.resultSetCacheProps && (
          <FlexItem breakpointMods={[{ modifier: 'flex-1', breakpoint: 'xl' }]}>
            <Card isHoverable={true} className="dv-metrics-container__card">
              <DvResultSetCacheMetric {...props.resultSetCacheProps} />
            </Card>
          </FlexItem>
        )}

        {props.uptimeProps && (
          <FlexItem breakpointMods={[{ modifier: 'flex-1', breakpoint: 'xl' }]}>
            <Card isHoverable={true} className="dv-metrics-container__card">
              <DvUptimeMetric {...props.uptimeProps} />
            </Card>
          </FlexItem>
        )}
      </Flex>
    );
  }

  return (
    <Bullseye>
      <EmptyState variant={EmptyStateVariant.small}>
        <Title headingLevel="h2" size="lg">
          {props.i18nNoDataTitle}
        </Title>
        <EmptyStateBody>
          <TextContent>
            <Text component={TextVariants.small}>
              {props.i18nNoDataDescription}
            </Text>
          </TextContent>
        </EmptyStateBody>
      </EmptyState>
    </Bullseye>
  );
};
