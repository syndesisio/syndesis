import {
  Card,
  CardBody
  ,
  Grid,
  GridItem,
  PageSection, CardTitle ,
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
        <Grid hasGutter>
          <GridItem span={6}>
            {this.props.definition && !this.props.definitionChildren ? (
              <Card>
                <CardTitle>This form definition object</CardTitle>
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
              <CardTitle>Creates this form</CardTitle>
              <CardBody>{this.props.children}</CardBody>
            </Card>
          </GridItem>
        </Grid>
      </PageSection>
    );
  }
}
