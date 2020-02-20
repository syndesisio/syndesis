import { EmptyState, EmptyStateBody, EmptyStateIcon, EmptyStateVariant, Title, Tooltip } from '@patternfly/react-core';
import { AddCircleOIcon } from '@patternfly/react-icons';
import * as H from '@syndesis/history';
import {
  ListView,
} from 'patternfly-react';
import * as React from 'react';
import { ButtonLink, PageSection } from '../../Layout';
import { IListViewToolbarProps, ListViewToolbar } from '../../Shared';

export interface IVirtualizationListProps extends IListViewToolbarProps {
  hasListData: boolean;
  i18nCreateDataVirtualization: string;
  i18nCreateDataVirtualizationTip?: string;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nImport: string;
  i18nImportTip: string;
  i18nLinkCreateVirtualization: string;
  i18nLinkCreateVirtualizationTip?: string;
  i18nName: string;
  i18nNameFilterPlaceholder: string;
  linkCreateHRef: H.LocationDescriptor;
  linkImportHRef: H.LocationDescriptor;
}

export const VirtualizationList: React.FunctionComponent<
  IVirtualizationListProps
> = props => {

  return (
    <>
      <PageSection noPadding={true} variant={'light'}>
        <ListViewToolbar {...props}>
          <div className="form-group">
            <Tooltip
              position={'top'}
              enableFlip={true}
              content={
                <div id={'importVirtualizationTip'}>
                  {props.i18nImportTip ? props.i18nImportTip : props.i18nImport}
                </div>
              }
            >
              <ButtonLink
                data-testid={'virtualization-list-import-button'}
                href={props.linkImportHRef}
                as={'default'}
              >
                {props.i18nImport}
              </ButtonLink>
            </Tooltip>
            <Tooltip
              position={'top'}
              enableFlip={true}
              content={
                <div id={'createVirtualizationTip'}>
                  {props.i18nLinkCreateVirtualizationTip
                    ? props.i18nLinkCreateVirtualizationTip
                    : props.i18nLinkCreateVirtualization}
                </div>
              }
            >
              <ButtonLink
                data-testid={'virtualization-list-create-virtualization-button'}
                href={props.linkCreateHRef}
                as={'primary'}
              >
                {props.i18nLinkCreateVirtualization}
              </ButtonLink>
            </Tooltip>
          </div>
        </ListViewToolbar>
      </PageSection>
      <PageSection noPadding={true} variant={'light'}>
        {props.hasListData ? (
          <ListView>{props.children}</ListView>
        ) : (
          <EmptyState variant={EmptyStateVariant.full}>
            <EmptyStateIcon icon={AddCircleOIcon} />
            <Title headingLevel="h5" size="lg">
              {props.i18nEmptyStateTitle}
            </Title>
            <EmptyStateBody>{props.i18nEmptyStateInfo}</EmptyStateBody>
            <Tooltip
              position={'top'}
              enableFlip={true}
              content={
                <div id={'createVirtualizationTip'}>
                  {props.i18nLinkCreateVirtualizationTip
                    ? props.i18nLinkCreateVirtualizationTip
                    : props.i18nLinkCreateVirtualization}
                </div>
              }
            >
              <ButtonLink
                data-testid={'virtualization-list-empty-state-create-button'}
                href={props.linkCreateHRef}
                as={'primary'}
              >
                {props.i18nLinkCreateVirtualization}
              </ButtonLink>
            </Tooltip>
          </EmptyState>
        )}
      </PageSection>
    </>
  );

}
