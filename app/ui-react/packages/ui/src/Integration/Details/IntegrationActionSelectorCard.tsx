// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import { Card, CardBody , Title, CardTitle  } from '@patternfly/react-core';
import * as React from 'react';

export interface IIntegrationActionSelectorCardProps {
  content: JSX.Element;
  title: string;
}

export const IntegrationActionSelectorCard: React.FunctionComponent<
  IIntegrationActionSelectorCardProps
> = ({ content, title }) => (
  <Card>
    <CardTitle>
      <Title className="syn-card__title" headingLevel="h2" size="md">{title}</Title>
    </CardTitle>
    <CardBody>{content}</CardBody>
  </Card>
);
