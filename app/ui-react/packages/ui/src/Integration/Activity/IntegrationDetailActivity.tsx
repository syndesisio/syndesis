import { Button, ListView } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { Container, PageSection } from '../../Layout';

export interface IIntegrationDetailActivityProps {
  i18nBtnRefresh: string;
  i18nLastRefresh: string;
  i18nViewLogOpenShift: string;
  linkToOpenShiftLog: string;
  onRefresh: () => void;
}

export class IntegrationDetailActivity extends React.Component<
  IIntegrationDetailActivityProps
> {
  public render() {
    return (
      <PageSection>
        <Container>
          <div className="pull-right">
            <Link to={this.props.linkToOpenShiftLog}>
              {this.props.i18nViewLogOpenShift}
            </Link>
            &nbsp;|&nbsp;
            {this.props.i18nLastRefresh}
            &nbsp;&nbsp;
            <Button onClick={this.props.onRefresh}>
              {this.props.i18nBtnRefresh}
            </Button>
          </div>
        </Container>
        {this.props.children ? (
          <ListView>{this.props.children}</ListView>
        ) : null}
      </PageSection>
    );
  }
}
