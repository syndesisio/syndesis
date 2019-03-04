import { Grid, Icon } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationStepsHorizontalViewProps {
  steps?: any;
}

export class IntegrationStepsHorizontalView extends React.Component<
  IIntegrationStepsHorizontalViewProps
> {
  public render() {
    return (
      <>
        <Grid fluid={true}>
          <Grid.Row className="show-grid">
            <Grid.Col xs={6} md={4}>
              {this.props.steps && this.props.steps[0] ? (
                <Icon name={'external-link'} />
              ) : null}
            </Grid.Col>
            {this.props.steps &&
              this.props.steps.map((opt: any) => (
                <>
                  <Grid.Col xs={6} md={4}>
                    <Icon name={'angle-right'} />
                  </Grid.Col>
                  <Grid.Col xsHidden={true} md={4}>
                    <span>
                      <p key={opt.id}>{opt.name}</p>
                    </span>
                  </Grid.Col>
                </>
              ))}
          </Grid.Row>
        </Grid>
        <Grid fluid={true}>
          {this.props.steps ? (
            <Grid.Row className="show-grid">
              <Grid.Col xs={2} md={2}>
                {<span>Step:</span>}
              </Grid.Col>
              <Grid.Col xs={10} md={10}>
                {this.props.steps ? <span>Goodbye</span> : null}
              </Grid.Col>
            </Grid.Row>
          ) : null}
        </Grid>
      </>
    );
  }
}
