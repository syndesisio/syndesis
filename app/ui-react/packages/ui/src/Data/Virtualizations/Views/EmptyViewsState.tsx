import { EmptyState, EmptyStateBody, EmptyStateIcon, EmptyStateVariant, Title, Tooltip } from '@patternfly/react-core';
import { AddCircleOIcon } from '@patternfly/react-icons';
import * as H from '@syndesis/history';
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

  return (
    <EmptyState variant={EmptyStateVariant.full}>
      <EmptyStateIcon icon={AddCircleOIcon} />
      <Title headingLevel="h5" size="lg">
        {props.i18nEmptyStateTitle}
      </Title>
      <EmptyStateBody>{props.i18nEmptyStateInfo}</EmptyStateBody>
      <>
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
            data-testid={'empty-views-state-import-views-button'}
            href={props.linkImportViewsHRef}
            as={'default'}
            className={'empty-views-import'}
          >
            {props.i18nImportViews}
          </ButtonLink>
        </Tooltip>
        <Tooltip
          position={'top'}
          enableFlip={true}
          content={
            <div id={'createViewTip'}>
              {props.i18nCreateViewTip
                ? props.i18nCreateViewTip
                : props.i18nCreateView}
            </div>
          }
        >
          <ButtonLink
            data-testid={'empty-views-state-create-view-button'}
            href={props.linkCreateViewHRef}
            as={'primary'}
          >
            {props.i18nCreateView}
          </ButtonLink>
        </Tooltip>
      </>
    </EmptyState>
  );
}
