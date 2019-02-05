import {
  EmptyState,
  ListView,
  OverlayTrigger,
  Tooltip,
} from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { IListViewToolbarProps, ListViewToolbar } from '../Shared';

export interface IExtensionListViewProps extends IListViewToolbarProps {
  i18nDescription: string;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nLinkImportExtension: string;
  i18nLinkImportExtensionTip?: string;
  i18nName: string;
  i18nNameFilterPlaceholder: string;
  i18nTitle: string;
  linkImportExtension: string;
}

export class CustomizationsExtensionListView extends React.Component<
  IExtensionListViewProps
> {
  public render() {
    return (
      <>
        <ListViewToolbar {...this.props}>
          <div className="form-group">
            <OverlayTrigger overlay={this.getImportTooltip()} placement="top">
              <Link
                to={this.props.linkImportExtension}
                className={'btn btn-primary'}
              >
                {this.props.i18nLinkImportExtension}
              </Link>
            </OverlayTrigger>
          </div>
        </ListViewToolbar>
        <div className="container-fluid">
          <h1>{this.props.i18nTitle}</h1>
          <p>{this.props.i18nDescription}</p>
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
                  overlay={this.getImportTooltip()}
                  placement="top"
                >
                  <Link
                    to={this.props.linkImportExtension}
                    className={'btn btn-primary'}
                  >
                    {this.props.i18nLinkImportExtension}
                  </Link>
                </OverlayTrigger>
              </EmptyState.Action>
            </EmptyState>
          )}
        </div>
      </>
    );
  }

  private getImportTooltip() {
    return (
      <Tooltip id="importTip">
        {this.props.i18nLinkImportExtensionTip
          ? this.props.i18nLinkImportExtensionTip
          : this.props.i18nLinkImportExtension}
      </Tooltip>
    );
  }
}
