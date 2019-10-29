import * as H from '@syndesis/history';
import {
  EmptyState,
  ListView,
  OverlayTrigger,
  Tooltip,
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

  const getCreateVirtualizationTooltip = (): JSX.Element => {
    return (
      <Tooltip id="createTip">
        {props.i18nLinkCreateVirtualizationTip
          ? props.i18nLinkCreateVirtualizationTip
          : props.i18nLinkCreateVirtualization}
      </Tooltip>
    );
  };

  return (
    <>
      <PageSection noPadding={true} variant={'light'}>
        <ListViewToolbar {...props}>
          <div className="form-group">
            <OverlayTrigger
              overlay={
                <Tooltip id="importTip">
                  {props.i18nImportTip}
                </Tooltip>
              }
              placement="top"
            >
              <ButtonLink
                data-testid={'virtualization-list-import-button'}
                href={props.linkImportHRef}
                as={'default'}
              >
                {props.i18nImport}
              </ButtonLink> 
            </OverlayTrigger>
            <OverlayTrigger
              overlay={getCreateVirtualizationTooltip()}
              placement="top"
            >
              <ButtonLink
                data-testid={
                  'virtualization-list-create-virtualization-button'
                }
                href={props.linkCreateHRef}
                as={'primary'}
              >
                {props.i18nLinkCreateVirtualization}
              </ButtonLink>
            </OverlayTrigger>
          </div>
        </ListViewToolbar>
      </PageSection>
      <PageSection noPadding={true} variant={'light'}>
        {props.hasListData ? (
          <ListView>{props.children}</ListView>
        ) : (
            <EmptyState>
              <EmptyState.Icon />
              <EmptyState.Title>
                {props.i18nEmptyStateTitle}
              </EmptyState.Title>
              <EmptyState.Info>{props.i18nEmptyStateInfo}</EmptyState.Info>
              <EmptyState.Action>
                <OverlayTrigger
                  overlay={getCreateVirtualizationTooltip()}
                  placement="top"
                >
                  <ButtonLink
                    data-testid={
                      'virtualization-list-empty-state-create-button'
                    }
                    href={props.linkCreateHRef}
                    as={'primary'}
                  >
                    {props.i18nLinkCreateVirtualization}
                  </ButtonLink>
                </OverlayTrigger>
              </EmptyState.Action>
            </EmptyState>
          )}
      </PageSection>
    </>
  );

}
