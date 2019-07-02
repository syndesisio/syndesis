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

export const EmptyViewsState: React.FunctionComponent<
  IEmptyViewsStateProps
> = props => {

  const getCreateViewTooltip = (): JSX.Element => {
    return (
      <Tooltip id="editTip">
        {props.i18nCreateViewTip ? props.i18nCreateViewTip : props.i18nCreateView}
      </Tooltip>
    );
  }

  const getImportViewsTooltip = (): JSX.Element => {
    return (
      <Tooltip id="importViewsTip">
        {props.i18nImportViewsTip
          ? props.i18nImportViewsTip
          : props.i18nImportViews}
      </Tooltip>
    );
  }

  return (
    <EmptyState>
      <EmptyState.Icon />
      <EmptyState.Title>{props.i18nEmptyStateTitle}</EmptyState.Title>
      <EmptyState.Info>{props.i18nEmptyStateInfo}</EmptyState.Info>
      <EmptyState.Action>
        <OverlayTrigger
          overlay={getImportViewsTooltip()}
          placement="top"
        >
          <ButtonLink
            data-testid={'empty-views-state-import-views-button'}
            href={props.linkImportViewsHRef}
            as={'default'}
            className={'empty-views-import'}
          >
            {props.i18nImportViews}
          </ButtonLink>
        </OverlayTrigger>
        <OverlayTrigger overlay={getCreateViewTooltip()} placement="top">
          <ButtonLink
            data-testid={'empty-views-state-create-view-button'}
            href={props.linkCreateViewHRef}
            as={'primary'}
          >
            {props.i18nCreateView}
          </ButtonLink>
        </OverlayTrigger>
      </EmptyState.Action>
    </EmptyState>
  );
}
