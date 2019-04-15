import * as H from 'history';
import {
  EmptyState,
  ListView,
  OverlayTrigger,
  Tooltip,
} from 'patternfly-react';
import * as React from 'react';
import { ButtonLink } from '../../../Layout';
import { IListViewToolbarProps, ListViewToolbar } from '../../../Shared';

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
  onImportView: (name: string) => void;
}

export class ViewList extends React.Component<IViewsListProps> {
  public constructor(props: IViewsListProps) {
    super(props);
  }

  public render() {
    return (
      <>
        <ListViewToolbar {...this.props}>
          <div className="form-group">
            <OverlayTrigger
              overlay={this.getImportViewsTooltip()}
              placement="top"
            >
              <ButtonLink href={this.props.linkImportViewsHRef} as={'default'}>
                {this.props.i18nImportViews}
              </ButtonLink>
            </OverlayTrigger>
            <OverlayTrigger
              overlay={this.getCreateViewTooltip()}
              placement="top"
            >
              <ButtonLink href={this.props.linkCreateViewHRef} as={'primary'}>
                {this.props.i18nCreateView}
              </ButtonLink>
            </OverlayTrigger>
          </div>
        </ListViewToolbar>
        {this.props.hasListData ? (
          <ListView>{this.props.children}</ListView>
        ) : (
          <EmptyState>
            <EmptyState.Icon />
            <EmptyState.Title>
              {this.props.i18nEmptyStateTitle}
            </EmptyState.Title>
            <EmptyState.Info>{this.props.i18nEmptyStateInfo}</EmptyState.Info>
            <EmptyState.Action>
              <OverlayTrigger
                overlay={this.getCreateViewTooltip()}
                placement="top"
              >
                <ButtonLink href={this.props.linkCreateViewHRef} as={'primary'}>
                  {this.props.i18nCreateView}
                </ButtonLink>
              </OverlayTrigger>
            </EmptyState.Action>
          </EmptyState>
        )}
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
