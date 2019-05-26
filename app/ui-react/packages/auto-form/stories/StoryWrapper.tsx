import { Card, CardBody, CardGrid } from 'patternfly-react';
import * as React from 'react';

export interface IStoryWrapperProps {
  children: any;
}

export class StoryWrapper extends React.Component<IStoryWrapperProps> {
  public render() {
    return (
      <div className="cards-pf">
        <CardGrid>
          <CardGrid.Row style={{ marginBottom: '20px', marginTop: '20px' }}>
            <CardGrid.Col xs={12} md={12}>
              <Card>
                <CardBody>{this.props.children}</CardBody>
              </Card>
            </CardGrid.Col>
          </CardGrid.Row>
        </CardGrid>
      </div>
    );
  }
}
