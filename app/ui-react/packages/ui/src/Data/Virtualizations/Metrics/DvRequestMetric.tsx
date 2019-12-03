import {
  Button,
  CardBody,
  CardHeader,
  Split,
  SplitItem,
  Stack,
  StackItem,
  Text,
  TextContent,
  TextVariants,
} from '@patternfly/react-core';
import { ErrorCircleOIcon, OkIcon } from '@patternfly/react-icons';
import { Spinner } from 'patternfly-react';
import * as React from 'react';
import './DvMetricsContainer.css';
import './DvRequestMetric.css';

/**
 * @property {string} a11yShowFailed - the localized accessibility text for the failed request icon
 * @property {string} a11yShowSucceeded - the localized accessibility text for the succeeded request icon
 * @property {number} failedCount - the number of failed requests
 * @property {string} i18nNoData - the localized text displayed when there is no metric data
 * @property {string} i18nTitle - the localize title of this metric
 * @property {boolean} loading - `true` when a backend call to fetch this metric is ongoing
 * @property {number} successCount - the number of successful requests
 * @property {() => void} onShowFailed - a callback for when the failed request icon is clicked
 * @property {() => void} onShowSucceeded - a callback for when the succeeded request icon is clicked
 */
export interface IDvRequestMetricProps {
  a11yShowFailed: string;
  a11yShowSucceeded: string;
  failedCount: number;
  i18nNoData: string;
  i18nTitle: string;
  loading: boolean;
  successCount: number;
  onShowFailed: () => void;
  onShowSucceeded: () => void;
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
        ) : props.failedCount && props.successCount ? (
          <Stack>
            <StackItem>
              <Split className={'dv-request-metric__content'} gutter={'lg'}>
                <SplitItem className={'dv-request-metric__count'}>
                  {props.successCount}
                </SplitItem>
                <SplitItem className={'dv-request-metric__countIcon'}>
                  <Button
                    variant="plain"
                    aria-label={props.a11yShowSucceeded}
                    onClick={props.onShowSucceeded}
                  >
                    <OkIcon color={'green'} />
                  </Button>
                </SplitItem>
              </Split>
            </StackItem>
            <StackItem>
              <Split className={'dv-request-metric__content'} gutter={'lg'}>
                <SplitItem className={'dv-request-metric__count'}>
                  {props.failedCount}
                </SplitItem>
                <SplitItem className={'dv-request-metric__countIcon'}>
                  <Button
                    variant="plain"
                    aria-label={props.a11yShowFailed}
                    onClick={props.onShowFailed}
                  >
                    <ErrorCircleOIcon color={'red'} />
                  </Button>
                </SplitItem>
              </Split>
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
