import { Grid } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailHistoryListViewProps {
  i18nTextDraft?: string;
  i18nTextHistory?: string;
}

export class IntegrationDetailHistoryListView extends React.Component<
  IIntegrationDetailHistoryListViewProps
> {
  public render() {
    return (
      <>
        <Grid fluid={true} key={1}>
          <Grid.Row className="show-grid">
            <Grid.Col xs={2} md={2}>
              {<span>{this.props.i18nTextDraft}:</span>}
            </Grid.Col>
            <Grid.Col xs={10} md={10}>
              <p>blah</p>
            </Grid.Col>
          </Grid.Row>

          <Grid.Row className="show-grid">
            <Grid.Col xs={2} md={2}>
              {<span>{this.props.i18nTextHistory}:</span>}
            </Grid.Col>
            <Grid.Col xs={10} md={10}>
              <p>blah</p>
            </Grid.Col>
          </Grid.Row>
        </Grid>
      </>
    );
  }
}
