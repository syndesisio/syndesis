import {
  CardBody,
  CardHeader,
  Spinner,
  Stack,
  StackItem,
  Text,
  TextContent,
  TextVariants,
} from '@patternfly/react-core';
import * as React from 'react';
import './DvMetricsContainer.css';
import './DvUptimeMetric.css';

/**
 * @property {string} i18nNoData - the localized text displayed when there is no metric data
 * @property {string} i18nSinceMessage - the localized text of the date the uptime started
 * @property {string} i18nTitle - the localize title of this metric
 * @property {string} i18nUptime - the localize text representing the total uptime
 * @property {boolean} loading - `true` when a backend call to fetch this metric is ongoing
 */
export interface IDvUptimeMetricProps {
  i18nNoData: string;
  i18nSinceMessage: string;
  i18nTitle: string;
  i18nUptime: string;
  loading: boolean;
}

/**
 * A component showing the uptime metric.
 * @param props the properties that configure this component
 */
export const DvUptimeMetric: React.FunctionComponent<
  IDvUptimeMetricProps
> = props => {
  return (
    <>
      <CardHeader className={'dv-metrics-container__cardTitle'}>
        {props.i18nTitle}
      </CardHeader>
      <CardBody>
        {props.loading ? (
          <Spinner size={'lg'} />
        ) : props.i18nSinceMessage &&
          props.i18nSinceMessage.length > 0 &&
          props.i18nUptime &&
          props.i18nUptime.length > 0 ? (
          <Stack className={'dv-uptime-metric__content'} gutter={'sm'}>
            <StackItem className={'dv-uptime-metric__sinceMessage'}>
              {props.i18nSinceMessage}
            </StackItem>
            <StackItem className={'dv-uptime-metric__uptimeMessage'}>
              {props.i18nUptime}
            </StackItem>
          </Stack>
        ) : (
          <TextContent>
            <Text component={TextVariants.small}>{props.i18nNoData}</Text>
          </TextContent>
        )}
      </CardBody>
    </>
  );
};
