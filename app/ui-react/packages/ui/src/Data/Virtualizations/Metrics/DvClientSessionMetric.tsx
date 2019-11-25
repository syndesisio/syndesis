import {
  Button,
  Card,
  CardActions,
  CardBody,
  CardHead,
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
 * @property {number} connectionCount - a count of the current number of connections
 * @property {string} i18nConnectionMessage - the localized text identifying the connections performing queries
 * @property {string} i18nNoData - the localized text displayed when there is no metric data
 * @property {string} i18nTitle - the localize title of this metric
 * @property {string} i18nViewAllAction - the localized text for the view all connections action
 * @property {boolean} loading - `true` when a backend call to fetch this metric is ongoing
 * @property {() => void} onViewAll - a callback for when the view all action is clicked
 */
export interface IDvClientSessionMetricProps {
  connectionCount: number;
  i18nConnectionMessage: string;
  i18nNoData: string;
  i18nTitle: string;
  i18nViewAllAction: string;
  loading: boolean;
  onViewAll: () => void;
}

/**
 * A component showing the client session metric.
 * @param props the properties that configure this component
 */
export const DvClientSessionMetric: React.FunctionComponent<
  IDvClientSessionMetricProps
> = props => {
  return (
    <Card isHoverable={true}>
      <CardHead>
        <CardActions>
          <Button onClick={props.onViewAll} variant={'link'}>
            {props.i18nViewAllAction}
          </Button>
        </CardActions>
        <CardHeader className={'dv-metrics-container__cardTitle'}>
          {props.i18nTitle}
        </CardHeader>
      </CardHead>
      <CardBody>
        {props.loading ? (
          <Spinner loading={true} inline={false} />
        ) : props.connectionCount &&
          props.i18nConnectionMessage &&
          props.i18nConnectionMessage.length > 0 ? (
          <Split className={'dv-client-session-metric__content'} gutter={'lg'}>
            <SplitItem className={'dv-client-session-metric__connectionCount'}>
              {props.connectionCount}
            </SplitItem>
            <SplitItem
              className={'dv-client-session-metric__connectionMessage'}
            >
              {props.i18nConnectionMessage}
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
