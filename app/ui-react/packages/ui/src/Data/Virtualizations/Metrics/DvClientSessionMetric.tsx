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
import './DvClientSessionMetric.css';
import './DvMetricsContainer.css';

/**
 * @property {number} sessionCount - a count of the current number of sessions
 * @property {string} i18nSessionText - the localized text suffix for sessions
 * @property {string} i18nNoData - the localized text displayed when there is no metric data
 * @property {string} i18nTitle - the localize title of this metric
 * @property {boolean} loading - `true` when a backend call to fetch this metric is ongoing
 */
export interface IDvClientSessionMetricProps {
  sessionCount: number;
  i18nSessionText: string;
  i18nNoData: string;
  i18nTitle: string;
  loading: boolean;
}

/**
 * A component showing the client session metric.
 * @param props the properties that configure this component
 */
export const DvClientSessionMetric: React.FunctionComponent<
  IDvClientSessionMetricProps
> = props => {
  return (
    <>
      <CardHeader className={'dv-metrics-container__cardTitle'}>
        {props.i18nTitle}
      </CardHeader>
      <CardBody>
        {props.loading ? (
          <Spinner loading={true} inline={false} />
        ) : props.sessionCount >= 0 &&
          props.i18nSessionText &&
          props.i18nSessionText.length > 0 ? (
          <Split
            className={'dv-client-session-metric__content'}
            gutter={'lg'}
          >
            <SplitItem
              className={'dv-client-session-metric__sessionCount'}
            >
              {props.sessionCount}
            </SplitItem>
            <SplitItem
              className={'dv-client-session-metric__sessionText'}
            >
              {props.i18nSessionText}
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
