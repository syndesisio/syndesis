import {
  Button,
  DataList
} from '@patternfly/react-core';
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

export const IntegrationDetailActivity: React.FC<
  IIntegrationDetailActivityProps
> = ( props ) => {
  return (
    <PageSection>
      <Container>
        <div className={'integration-detail-activity-toolbar pull-right'}>
          {props.linkToOpenShiftLog && (
            <>
              <Link
                data-testid={'integration-detail-activity-view-log-link'}
                to={props.linkToOpenShiftLog}
              >
                {props.i18nViewLogOpenShift}
              </Link>
              &nbsp;|&nbsp;
            </>
          )}
          <span className={'integration-detail-activity-toolbar-last-refresh'}>
              {props.i18nLastRefresh}
            </span>
          &nbsp;&nbsp;
          <Button
            data-testid={'integration-detail-activity-refresh-button'}
            onClick={props.onRefresh}
          >
            {props.i18nBtnRefresh}
          </Button>
        </div>
      </Container>
      {props.children ? (
        <DataList aria-label={'integration detail activity list'}>{props.children}</DataList>
      ) : null}
    </PageSection>
  );
};
