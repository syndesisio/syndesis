import * as H from 'history';
import {
  Button,
  EmptyState,
  ListView,
  OverlayTrigger,
  Tooltip,
} from 'patternfly-react';
import * as React from 'react';
import { ButtonLink } from '../../../../Layout';
import { IListViewToolbarProps, ListViewToolbar } from '../../../../Shared';

export interface IViewsListProps extends IListViewToolbarProps {
  i18nCreateView: string;
  i18nCreateViewTip?: string;
  i18nDescription: string;
  hasListData: boolean;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nImportView: string;
  i18nImportViewTip: string;
  linkCreateHRef: H.LocationDescriptor;
  i18nLinkCreateView: string;
  i18nLinkCreateViewTip?: string;
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
              overlay={this.getImportViewTooltip()}
              placement="top"
            >
              <Button
                bsStyle="default"
                to={this.props.i18nImportView}
                onClick={this.handleImportView}
              >
                {this.props.i18nImportView}
              </Button>
            </OverlayTrigger>
            <OverlayTrigger
              overlay={this.getCreateViewTooltip()}
              placement="top"
            >
              <ButtonLink href={this.props.linkCreateHRef} as={'primary'}>
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
                <ButtonLink href={this.props.linkCreateHRef} as={'primary'}>
                  {this.props.i18nLinkCreateView}
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
        {this.props.i18nLinkCreateViewTip
          ? this.props.i18nLinkCreateViewTip
          : this.props.i18nLinkCreateView}
      </Tooltip>
    );
  }

  private getImportViewTooltip() {
    return (
      <Tooltip id="importViewTip">
        {this.props.i18nImportViewTip
          ? this.props.i18nImportViewTip
          : this.props.i18nImportView}
      </Tooltip>
    );
  }

  private handleImportView = () => {
    this.props.onImportView('');
  };
}
