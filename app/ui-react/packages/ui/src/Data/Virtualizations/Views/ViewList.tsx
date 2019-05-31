import * as H from '@syndesis/history';
import { ListView, OverlayTrigger, Tooltip } from 'patternfly-react';
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
  i18nDescription: string;
  i18nName: string;
  i18nNameFilterPlaceholder: string;
}

export class ViewList extends React.Component<IViewsListProps> {
  public constructor(props: IViewsListProps) {
    super(props);
  }

  public render() {
    return (
      <>
        <PageSection noPadding={true} variant={'light'}>
          <ListViewToolbar {...this.props}>
            <div className="form-group">
              <OverlayTrigger
                overlay={this.getImportViewsTooltip()}
                placement="top"
              >
                <ButtonLink
                  data-testid={'view-list-import-views-button'}
                  href={this.props.linkImportViewsHRef}
                  as={'default'}
                >
                  {this.props.i18nImportViews}
                </ButtonLink>
              </OverlayTrigger>
              <OverlayTrigger
                overlay={this.getCreateViewTooltip()}
                placement="top"
              >
                <ButtonLink
                  data-testid={'view-list-create-view-button'}
                  href={this.props.linkCreateViewHRef}
                  as={'primary'}
                >
                  {this.props.i18nCreateView}
                </ButtonLink>
              </OverlayTrigger>
            </div>
          </ListViewToolbar>
        </PageSection>
        <PageSection noPadding={true} variant={'light'}>
          {this.props.hasListData ? (
            <ListView>{this.props.children}</ListView>
          ) : (
            <EmptyViewsState
              i18nEmptyStateTitle={this.props.i18nEmptyStateTitle}
              i18nEmptyStateInfo={this.props.i18nEmptyStateInfo}
              i18nCreateView={this.props.i18nCreateView}
              i18nCreateViewTip={this.props.i18nCreateViewTip}
              i18nImportViews={this.props.i18nImportViews}
              i18nImportViewsTip={this.props.i18nImportViewsTip}
              linkCreateViewHRef={this.props.linkCreateViewHRef}
              linkImportViewsHRef={this.props.linkImportViewsHRef}
            />
          )}
        </PageSection>
      </>
    );
  }

  private getCreateViewTooltip() {
    return (
      <Tooltip id="createTip">
        {this.props.i18nCreateViewTip
          ? this.props.i18nCreateViewTip
          : this.props.i18nCreateView}
      </Tooltip>
    );
  }

  private getImportViewsTooltip() {
    return (
      <Tooltip id="importViewsTip">
        {this.props.i18nImportViewsTip
          ? this.props.i18nImportViewsTip
          : this.props.i18nImportViews}
      </Tooltip>
    );
  }
}
