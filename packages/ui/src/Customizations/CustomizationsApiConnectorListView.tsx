import { EmptyState } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { IListViewToolbarProps, ListViewToolbar } from '../ListViewToolbar';

export interface IApiConnectorListViewProps extends IListViewToolbarProps {
  i18nDescription: string;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nLinkCreateApiConnector: string;
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
            <Link
              to={this.props.linkCreateApiConnector}
              className={'btn btn-primary'}
            >
              {this.props.i18nLinkCreateApiConnector}
            </Link>
          </div>
        </ListViewToolbar>
        {this.props.children ? (
          <div className={'container-fluid'}>{this.props.children}</div>
        ) : (
          <EmptyState>
            <EmptyState.Icon />
            <EmptyState.Title>
              {this.props.i18nEmptyStateTitle}
            </EmptyState.Title>
            <EmptyState.Info>{this.props.i18nEmptyStateInfo}</EmptyState.Info>
            <EmptyState.Action>
              <Link
                to={this.props.linkCreateApiConnector}
                className={'btn btn-primary'}
              >
                {this.props.i18nLinkCreateApiConnector}
              </Link>
            </EmptyState.Action>
          </EmptyState>
        )}
      </>
    );
  }
}
