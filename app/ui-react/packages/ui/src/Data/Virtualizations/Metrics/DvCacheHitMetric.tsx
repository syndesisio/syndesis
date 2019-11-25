import {
  Card,
  CardBody,
  CardHead,
  CardHeader,
  Popover,
  Split,
  SplitItem,
  Text,
  TextContent,
  TextVariants,
} from '@patternfly/react-core';
import { InfoCircleIcon } from '@patternfly/react-icons';
import { Spinner } from 'patternfly-react';
import * as React from 'react';
import './DvCacheHitMetric.css';
import './DvMetricsContainer.css';

/**
 * @property {string} a11yInfoCloseButton - the localized accessibility text for the info popover close button
 * @property {string} a11yInfoPopover - the localized accessibility text for the info popover
 * @property {string} i18nDatetime - the localized text representing the time of the metric was taken
 * @property {string} i18nInfoMessage - the localized text of the message shown in the info popover
 * @property {string} i18nNoData - the localized text displayed when there is no metric data
 * @property {string} i18nTitle - the localize title of this metric
 * @property {boolean} loading - `true` when a backend call to fetch this metric is ongoing
 * @property {string} percentage - the text representing the cache hit ratio
 */
export interface IDvCacheHitMetricProps {
  a11yInfoCloseButton: string;
  a11yInfoPopover: string;
  i18nDatetime: string;
  i18nInfoMessage: string;
  i18nNoData: string;
  i18nTitle: string;
  loading: boolean;
  percentage: string;
}

/**
 * A component showing the cache hit ratio metric.
 * @param props the properties that configure this component
 */
export const DvCacheHitMetric: React.FunctionComponent<
  IDvCacheHitMetricProps
> = props => {
  return (
    <Card isHoverable={true}>
      <CardHead>
        <CardHeader className={'dv-metrics-container__cardTitle'}>
          {props.i18nTitle}
          &nbsp;&nbsp;
          <Popover
            aria-label={props.a11yInfoPopover}
            bodyContent={<div>{props.i18nInfoMessage}</div>}
            closeBtnAriaLabel={props.a11yInfoCloseButton}
          >
            <InfoCircleIcon color={'blue'} />
          </Popover>
        </CardHeader>
      </CardHead>
      <CardBody>
        {props.loading ? (
          <Spinner loading={true} inline={false} />
        ) : props.percentage &&
          props.percentage.length > 0 &&
          props.i18nDatetime &&
          props.i18nDatetime.length > 0 ? (
          <Split className={'dv-cache-hit-metric__content'} gutter={'lg'}>
            <SplitItem className={'dv-cache-hit-metric__percentage'}>
              {props.percentage}
            </SplitItem>
            <SplitItem className={'dv-cache-hit-metric__datetime'}>
              {props.i18nDatetime}
            </SplitItem>
          </Split>
        ) : (
          <TextContent>
            <Text component={TextVariants.small}>{props.i18nNoData}</Text>
          </TextContent>
        )}
      </CardBody>
    </Card>
  );
};
