import {
  Button,
  Grid,
  Icon,
  ListView,
  ListViewIcon,
  ListViewItem,
} from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailTabProps {
  description?: string;
  integrationIsDraft: boolean;
  i18nTextBtnEdit?: string;
  i18nTextBtnPublish?: string;
  i18nTextDraft?: string;
  i18nTextHistory?: string;
  steps?: any;
}

export class IntegrationDetailTab extends React.PureComponent<
  IIntegrationDetailTabProps
> {
  public render() {
    return (
      <>
        <Grid fluid={true} xs={4}>
          <Grid.Row className="show-grid">
            <Grid.Col xs={6} md={4}>
              {this.props.steps && this.props.steps[0] ? (
                <>
                  <ListViewIcon name={'cube'} />
                  <span>
                    <p>{this.props.steps[0].name}</p>
                  </span>
                </>
              ) : null}
            </Grid.Col>
            {this.props.steps &&
              this.props.steps.slice(1).map((opt: any, index: any) => (
                <>
                  <Grid.Col xs={6} md={4}>
                    <Icon name={'angle-right'} />
                  </Grid.Col>
                  <Grid.Col xsHidden={true} md={4}>
                    <span>
                      <ListViewIcon name={'cube'} />
                      <p key={index}>{opt.name}</p>
                    </span>
                  </Grid.Col>
                </>
              ))}
          </Grid.Row>
        </Grid>
        <div>
          {this.props.description ? (
            <p>
              {this.props.description}&nbsp;
              <Icon name={'pencil'} />
            </p>
          ) : null}
        </div>
        <Grid fluid={true} key={1}>
          {this.props.integrationIsDraft ? (
            <Grid.Row className="show-grid">
              <Grid.Col xs={2} md={2}>
                {this.props.i18nTextDraft}:
              </Grid.Col>
              <Grid.Col xs={10} md={10}>
                <ListViewItem
                  key={1}
                  heading={this.props.i18nTextDraft}
                  actions={
                    <>
                      <Button>{this.props.i18nTextBtnPublish}</Button>
                      <Button>{this.props.i18nTextBtnEdit}</Button>
                    </>
                  }
                  stacked={false}
                />
              </Grid.Col>
            </Grid.Row>
          ) : null}

          {this.props.children ? (
            <Grid.Row className="show-grid">
              <Grid.Col xs={2} md={2}>
                {<span>{this.props.i18nTextHistory}:</span>}
              </Grid.Col>
              <Grid.Col xs={10} md={10}>
                {this.props.children ? (
                  <ListView>{this.props.children}</ListView>
                ) : null}
              </Grid.Col>
            </Grid.Row>
          ) : null}
        </Grid>
      </>
    );
  }
}
