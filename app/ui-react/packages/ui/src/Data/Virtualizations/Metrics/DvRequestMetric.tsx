import {
  CardBody,
  CardHeader,
  Split,
  SplitItem,
  Text,
  TextContent,
  TextVariants,
} from '@patternfly/react-core';
import { Spinner } from 'patternfly-react';
import * as React from 'react';
import './DvMetricsContainer.css';
import './DvRequestMetric.css';

/**
 * @property {string} i18nNoData - the localized text displayed when there is no metric data
 * @property {string} i18nRequestText - the localized text suffix for requests
 * @property {string} i18nTitle - the localize title of this metric
 * @property {boolean} loading - `true` when a backend call to fetch this metric is ongoing
 * @property {number} requestCount - the number of total requests
 */
export interface IDvRequestMetricProps {
  i18nNoData: string;
  i18nRequestText: string;
  i18nTitle: string;
  loading: boolean;
  requestCount: number;
}

/**
 * A component showing the request metric.
 * @param props the properties that configure this component
 */
export const DvRequestMetric: React.FunctionComponent<
  IDvRequestMetricProps
> = props => {
  return (
    <>
      <CardHeader className={'dv-metrics-container__cardTitle'}>
        {props.i18nTitle}
      </CardHeader>
      <CardBody>
        {props.loading ? (
          <Spinner loading={true} inline={false} />
        ) : props.requestCount &&
          props.i18nRequestText &&
          props.i18nRequestText.length > 0 ? (
          <Split className={'dv-request-metric__content'} gutter={'lg'}>
            <SplitItem className={'dv-request-metric__requestCount'}>
              {props.requestCount}
            </SplitItem>
            <SplitItem className={'dv-request-metric__requestText'}>
              {props.i18nRequestText}
            </SplitItem>
          </Split>
        ) : (
          <TextContent>
            <Text component={TextVariants.small}>{props.i18nNoData}</Text>
          </TextContent>
        )}
      </CardBody>
    </>
  );
};
