import { Button, ListView } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { Container, PageSection } from '../../Layout';
import './IntegrationDetailActivity.css';

export interface IIntegrationDetailActivityProps {
  i18nBtnRefresh: string;
  i18nLastRefresh: string;
  i18nViewLogOpenShift: string;
  linkToOpenShiftLog?: string;
  onRefresh: () => void;
}

export class IntegrationDetailActivity extends React.Component<
  IIntegrationDetailActivityProps
> {
  public render() {
    return (
      <PageSection>
        <Container>
          <div className="integration-detail-activity-toolbar pull-right">
            {this.props.linkToOpenShiftLog && (
              <>
                <Link
                  data-testid={'integration-detail-activity-view-log-link'}
                  to={this.props.linkToOpenShiftLog}
                >
                  {this.props.i18nViewLogOpenShift}
                </Link>
                &nbsp;|&nbsp;
              </>
            )}
            <span className="integration-detail-activity-toolbar-last-refresh">
              {this.props.i18nLastRefresh}
            </span>
            &nbsp;&nbsp;
            <Button
              data-testid={'integration-detail-activity-refresh-button'}
              onClick={this.props.onRefresh}
            >
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
