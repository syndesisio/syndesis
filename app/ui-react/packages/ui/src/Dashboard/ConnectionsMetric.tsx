import {
  Card,
  CardBody,
  Stack,
  StackItem,
  Title,
} from '@patternfly/react-core';
import * as React from 'react';
import './ConnectionsMetric.css';

export interface IConnectionsMetricProps {
  i18nTitle: string;
}

export const ConnectionsMetric: React.FunctionComponent<IConnectionsMetricProps> =
  ({ i18nTitle }) => (
    <Card
      data-testid={'dashboard-page-total-connections'}
      className={'aggregate-status'}
    >
      <CardBody>
        <Stack>
          <StackItem>
            <br />
          </StackItem>
          <StackItem isFilled={true}>
            <br />
            <Title size={'lg'} headingLevel={'h3'}>
              {i18nTitle}
            </Title>
          </StackItem>
          <StackItem>
            <br />
          </StackItem>
        </Stack>
      </CardBody>
    </Card>
  );
