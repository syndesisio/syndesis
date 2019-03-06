import {
  Card,
  CardBody,
  CardGrid,
  CardHeading,
  CardTitle,
} from 'patternfly-react';
import * as React from 'react';
import { ApiConnectorDetailCard } from './ApiConnectorDetailCard';
import { ApiConnectorInfo } from './ApiConnectorInfo';
import { ApiConnectorReview } from './ApiConnectorReview';

export interface IApiConnectorDetailProps {
  description?: string;
  icon?: string;
  name: string;
}

export class ApiConnectorDetail extends React.Component<
  IApiConnectorDetailProps
> {
  public render() {
    return (
      <CardGrid fluid={true}>
        <CardGrid.Row>
          <CardGrid.Col xs={12} md={3}>
            <ApiConnectorDetailCard
              description={this.props.description}
              icon={this.props.icon}
              name={this.props.name}
            />
          </CardGrid.Col>
          <CardGrid.Col xs={12} md={7}>
            <Card>
              <CardHeading>
                <CardTitle>{this.props.name}</CardTitle>
              </CardHeading>
              <CardBody>
                <ApiConnectorInfo />
                <ApiConnectorReview />
              </CardBody>
            </Card>
          </CardGrid.Col>
        </CardGrid.Row>
      </CardGrid>
    );
  }
}
