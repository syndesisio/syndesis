import {
  Card,
  CardBody,
  CardHeader,
  Grid,
  GridItem,
  PageSection,
} from '@patternfly/react-core';
import * as React from 'react';

export interface IStoryWrapperProps {
  definition?: any;
  definitionChildren?: JSX.Element;
  children: any;
}

export class StoryWrapper extends React.Component<IStoryWrapperProps> {
  public render() {
    return (
      <PageSection>
        <Grid gutter={'md'}>
          <GridItem span={6}>
            {this.props.definition && !this.props.definitionChildren ? (
              <Card>
                <CardHeader>This form definition object</CardHeader>
                <CardBody>
                  <pre>
                    {JSON.stringify(this.props.definition, undefined, 2)}
                  </pre>
                </CardBody>
              </Card>
            ) : (
              <Card>
                <CardBody>{this.props.definitionChildren}</CardBody>
              </Card>
            )}
          </GridItem>
          <GridItem span={6}>
            <Card>
              <CardHeader>Creates this form</CardHeader>
              <CardBody>{this.props.children}</CardBody>
            </Card>
          </GridItem>
        </Grid>
      </PageSection>
    );
  }
}
