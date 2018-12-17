import {
  EmptyState,
  ListView,
  OverlayTrigger,
  Tooltip,
} from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { IListViewToolbarProps, ListViewToolbar } from '../Shared';

export interface IApiConnectorListViewProps extends IListViewToolbarProps {
  i18nDescription: string;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nLinkCreateApiConnector: string;
  i18nLinkCreateApiConnectorTip?: string;
  i18nName: string;
  i18nNameFilterPlaceholder: string;
  i18nTitle: string;
  linkCreateApiConnector: string;
}

export class CustomizationsApiConnectorListView extends React.Component<
  IApiConnectorListViewProps
> {
  public render() {
    return (
      <>
        <h2>{this.props.i18nTitle}</h2>
        <h3>{this.props.i18nDescription}</h3>
        <ListViewToolbar {...this.props}>
          <div className="form-group">
            <OverlayTrigger
              overlay={this.getCreateConnectorTooltip()}
              placement="top"
            >
              <Link
                to={this.props.linkCreateApiConnector}
                className={'btn btn-primary'}
              >
                {this.props.i18nLinkCreateApiConnector}
              </Link>
            </OverlayTrigger>
          </div>
        </ListViewToolbar>
        {this.props.children ? (
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
                overlay={this.getCreateConnectorTooltip()}
                placement="top"
              >
                <Link
                  to={this.props.linkCreateApiConnector}
                  className={'btn btn-primary'}
                >
                  {this.props.i18nLinkCreateApiConnector}
                </Link>
              </OverlayTrigger>
            </EmptyState.Action>
          </EmptyState>
        )}
      </>
    );
  }

  private getCreateConnectorTooltip() {
    return (
      <Tooltip id="createTip">
        {this.props.i18nLinkCreateApiConnectorTip
          ? this.props.i18nLinkCreateApiConnectorTip
          : this.props.i18nLinkCreateApiConnector}
      </Tooltip>
    );
  }
}
