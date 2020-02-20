import { Tooltip } from '@patternfly/react-core';
import * as H from '@syndesis/history';
import { ListView } from 'patternfly-react';
import * as React from 'react';
import { ButtonLink, PageSection } from '../../../Layout';
import { IListViewToolbarProps, ListViewToolbar } from '../../../Shared';
import { EmptyViewsState } from './EmptyViewsState';
import './ViewList.css';

export interface IViewsListProps extends IListViewToolbarProps {
  hasListData: boolean;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nImportViews: string;
  i18nImportViewsTip: string;
  linkCreateViewHRef: H.LocationDescriptor;
  linkImportViewsHRef: H.LocationDescriptor;
  i18nCreateViewTip?: string;
  i18nCreateView: string;
  i18nName: string;
  i18nNameFilterPlaceholder: string;
}

export const ViewList: React.FunctionComponent<IViewsListProps> = props => {
  return (
    <PageSection>
      {props.hasListData ? (
        <React.Fragment>
          <ListViewToolbar {...props}>
            <div className="form-group">
              <Tooltip
                position={'top'}
                enableFlip={true}
                content={
                  <div id={'importViewsTip'}>
                    {props.i18nImportViewsTip
                      ? props.i18nImportViewsTip
                      : props.i18nImportViews}
                  </div>
                }
              >
                <ButtonLink
                  data-testid={'view-list-import-views-button'}
                  href={props.linkImportViewsHRef}
                  as={'default'}
                >
                  {props.i18nImportViews}
                </ButtonLink>
              </Tooltip>
              <Tooltip
                position={'top'}
                enableFlip={true}
                content={
                  <div id={'createViewsTip'}>
                    {props.i18nCreateViewTip
                      ? props.i18nCreateViewTip
                      : props.i18nCreateView}
                  </div>
                }
              >
                <ButtonLink
                  data-testid={'view-list-create-view-button'}
                  href={props.linkCreateViewHRef}
                  as={'primary'}
                >
                  {props.i18nCreateView}
                </ButtonLink>
              </Tooltip>
            </div>
          </ListViewToolbar>
          <ListView>{props.children}</ListView>
        </React.Fragment>
      ) : (
        <EmptyViewsState
          i18nEmptyStateTitle={props.i18nEmptyStateTitle}
          i18nEmptyStateInfo={props.i18nEmptyStateInfo}
          i18nCreateView={props.i18nCreateView}
          i18nCreateViewTip={props.i18nCreateViewTip}
          i18nImportViews={props.i18nImportViews}
          i18nImportViewsTip={props.i18nImportViewsTip}
          linkCreateViewHRef={props.linkCreateViewHRef}
          linkImportViewsHRef={props.linkImportViewsHRef}
        />
      )}
    </PageSection>
  );
};
