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
  i18nTitle: string;
  linkCreateApiConnector: string;
}

export class ApiConnectorListView extends React.Component<
  IApiConnectorListViewProps
> {
  public render() {
    return (
      <>
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
        <div className="container-fluid">
          <h1>{this.props.i18nTitle}</h1>
          <div
            dangerouslySetInnerHTML={{ __html: this.props.i18nDescription }}
          />
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
        </div>
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
