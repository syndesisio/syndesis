import {
  Bullseye,
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
import { DvCacheHitMetric, IDvCacheHitMetricProps } from './DvCacheHitMetric';
import {
  DvClientSessionMetric,
  IDvClientSessionMetricProps,
} from './DvClientSessionMetric';
import { DvRequestMetric, IDvRequestMetricProps } from './DvRequestMetric';
import { DvUptimeMetric, IDvUptimeMetricProps } from './DvUptimeMetric';

/**
 * A component container for DV metrics.
 * @property {IDvCacheHitMetricProps} cacheHitProps - if set, the cache hit ratio metric is shown
 * @property {IDvClientSessionMetricProps} clientSessionProps - if set, the clien session metric component is shown
 * @property {string} i18nNoData - the localized text used when no metric components are used
 * @property {IDvRequestMetricProps} requestProps - if set, the request metric component is shown
 * @property {IDvUptimeMetricProps} uptimeProps - if set, the uptime metric component is shown
 */
export interface IDvMetricsContainer {
  cacheHitProps?: IDvCacheHitMetricProps;
  clientSessionProps?: IDvClientSessionMetricProps;
  i18nNoDataTitle: string;
  i18nNoDataDescription: string;
  requestProps?: IDvRequestMetricProps;
  uptimeProps?: IDvUptimeMetricProps;
}

export const DvMetricsContainer: React.FunctionComponent<
  IDvMetricsContainer
> = props => {
  if (
    props.cacheHitProps ||
    props.clientSessionProps ||
    props.requestProps ||
    props.uptimeProps
  ) {
    return (
      <Flex>
        {props.clientSessionProps && (
          <FlexItem className={'pf-m-flex-1'}>
            <DvClientSessionMetric {...props.clientSessionProps} />
          </FlexItem>
        )}
        {props.requestProps && (
          <FlexItem className={'pf-m-flex-1'}>
            <DvRequestMetric {...props.requestProps} />
          </FlexItem>
        )}
        {props.cacheHitProps && (
          <FlexItem className={'pf-m-flex-1'}>
            <DvCacheHitMetric {...props.cacheHitProps} />
          </FlexItem>
        )}
        {props.uptimeProps && (
          <FlexItem className={'pf-m-flex-1'}>
            <DvUptimeMetric {...props.uptimeProps} />
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
            <Text component={TextVariants.small}>{props.i18nNoDataDescription}</Text>
          </TextContent>
        </EmptyStateBody>
      </EmptyState>
    </Bullseye>
  );
};
