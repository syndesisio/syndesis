import { Button, ListView } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { Container } from '../Layout';

export interface IIntegrationDetailActivityProps {
  i18nBtnRefresh: string;
  i18nLastRefresh: string;
  i18nViewLogOpenShift: string;
  linkToOpenShiftLog: string;
}

export class IntegrationDetailActivity extends React.Component<
  IIntegrationDetailActivityProps
> {
  public render() {
    return (
      <>
        <Container>
          <div className="pull-right">
            <Link to={this.props.linkToOpenShiftLog}>
              {this.props.i18nViewLogOpenShift}
            </Link>
            {'  |  '}
            {this.props.i18nLastRefresh}
            {'  '}
            <Button>{this.props.i18nBtnRefresh}</Button>
          </div>
        </Container>
        {this.props.children ? (
          <ListView>{this.props.children}</ListView>
        ) : null}
      </>
    );
  }
}
