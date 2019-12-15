import {
  CardBody,
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
import './DvMetricsContainer.css';
import './DvResultSetCacheMetric.css';

/**
 * @property {string} a11yInfoCloseButton - the localized accessibility text for the info popover close button
 * @property {string} a11yInfoPopover - the localized accessibility text for the info popover
 * @property {string} i18nCacheHitRatioText - the localized text representing the hit ratio percentage
 * @property {string} i18nInfoMessage - the localized text of the message shown in the info popover
 * @property {string} i18nNoData - the localized text displayed when there is no metric data
 * @property {string} i18nTitle - the localize title of this metric
 * @property {boolean} loading - `true` when a backend call to fetch this metric is ongoing
 * @property {string} cacheHitRatioPercentage - the text representing the cache hit ratio percentage
 */
export interface IDvResultSetCacheMetricProps {
  a11yInfoCloseButton: string;
  a11yInfoPopover: string;
  i18nCacheHitRatioText: string;
  i18nInfoMessage: string;
  i18nNoData: string;
  i18nTitle: string;
  loading: boolean;
  cacheHitRatioPercentage: string;
}

/**
 * A component showing the result set cache metric.
 * @param props the properties that configure this component
 */
export const DvResultSetCacheMetric: React.FunctionComponent<
  IDvResultSetCacheMetricProps
> = props => {
  return (
    <>
      <CardHeader className={'dv-metrics-container__cardTitle'}>
        {props.i18nTitle}
      </CardHeader>
      <CardBody>
        {props.loading ? (
          <Spinner loading={true} inline={false} />
        ) : props.cacheHitRatioPercentage &&
          props.cacheHitRatioPercentage.length > 0 &&
          props.i18nCacheHitRatioText &&
          props.i18nCacheHitRatioText.length > 0 ? (
          <Split className={'dv-result-set-cache-metric__content'} gutter={'lg'}>
            <SplitItem className={'dv-result-set-cache-metric__hitRatioPercentage'}>
              {props.cacheHitRatioPercentage}
            </SplitItem>
            <SplitItem className={'dv-result-set-cache-metric__hitRatioText'}>
              {props.i18nCacheHitRatioText}
            </SplitItem>
            <SplitItem>
              <Popover
                aria-label={props.a11yInfoPopover}
                bodyContent={<div>{props.i18nInfoMessage}</div>}
                closeBtnAriaLabel={props.a11yInfoCloseButton}
              >
                <InfoCircleIcon color={'blue'} />
              </Popover>
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
