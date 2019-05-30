import * as H from '@syndesis/history';
import { EmptyState, OverlayTrigger, Tooltip } from 'patternfly-react';
import * as React from 'react';
import { ButtonLink } from '../../../Layout';
import './EmptyViewsState.css';

export interface IEmptyViewsStateProps {
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nImportViews: string;
  i18nImportViewsTip: string;
  linkCreateViewHRef: H.LocationDescriptor;
  linkImportViewsHRef: H.LocationDescriptor;
  i18nCreateViewTip?: string;
  i18nCreateView: string;
}

export class EmptyViewsState extends React.Component<IEmptyViewsStateProps> {
  public render() {
    return (
      <EmptyState>
        <EmptyState.Icon />
        <EmptyState.Title>{this.props.i18nEmptyStateTitle}</EmptyState.Title>
        <EmptyState.Info>{this.props.i18nEmptyStateInfo}</EmptyState.Info>
        <EmptyState.Action>
          <OverlayTrigger
            overlay={this.getImportViewsTooltip()}
            placement="top"
          >
            <ButtonLink
              data-testid={'empty-views-state-import-views-button'}
              href={this.props.linkImportViewsHRef}
              as={'default'}
              className={'empty-views-import'}
            >
              {this.props.i18nImportViews}
            </ButtonLink>
          </OverlayTrigger>
          <OverlayTrigger overlay={this.getCreateViewTooltip()} placement="top">
            <ButtonLink
              data-testid={'empty-views-state-create-view-button'}
              href={this.props.linkCreateViewHRef}
              as={'primary'}
            >
              {this.props.i18nCreateView}
            </ButtonLink>
          </OverlayTrigger>
        </EmptyState.Action>
      </EmptyState>
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
