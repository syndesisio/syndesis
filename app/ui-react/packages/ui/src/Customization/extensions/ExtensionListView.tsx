import { Text, Title } from '@patternfly/react-core';
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

export interface IExtensionListViewProps extends IListViewToolbarProps {
  i18nDescription: string;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nLinkImportExtension: H.LocationDescriptor;
  i18nLinkImportExtensionTip?: H.LocationDescriptor;
  i18nName: string;
  i18nNameFilterPlaceholder: string;
  i18nTitle: string;
  linkImportExtension: H.LocationDescriptor;
}

export const ExtensionListView: React.FunctionComponent<
  IExtensionListViewProps
> = props => {
  const getImportTooltip = (): JSX.Element => {
    return (
      <Tooltip id="importTip">
        {props.i18nLinkImportExtensionTip
          ? props.i18nLinkImportExtensionTip
          : props.i18nLinkImportExtension}
      </Tooltip>
    );
  };

  return (
    <PageSection>
      <ListViewToolbar {...props}>
        <div className="form-group">
          <OverlayTrigger overlay={getImportTooltip()} placement="top">
            <ButtonLink
              data-testid={'extension-list-view-import-button'}
              href={props.linkImportExtension}
              as={'primary'}
            >
              {props.i18nLinkImportExtension}
            </ButtonLink>
          </OverlayTrigger>
        </div>
      </ListViewToolbar>
      {props.i18nTitle !== '' && <Title size="lg">{props.i18nTitle}</Title>}
      {props.i18nDescription !== '' && (
        <Text dangerouslySetInnerHTML={{ __html: props.i18nDescription }} />
      )}
      {props.children ? (
        <ListView>{props.children}</ListView>
      ) : (
        <EmptyState>
          <EmptyState.Icon />
          <EmptyState.Title>{props.i18nEmptyStateTitle}</EmptyState.Title>
          <EmptyState.Info>{props.i18nEmptyStateInfo}</EmptyState.Info>
          <EmptyState.Action>
            <OverlayTrigger overlay={getImportTooltip()} placement="top">
              <ButtonLink
                data-testid={'extension-list-view-empty-state-import-button'}
                href={props.linkImportExtension}
                as={'primary'}
              >
                {props.i18nLinkImportExtension}
              </ButtonLink>
            </OverlayTrigger>
          </EmptyState.Action>
        </EmptyState>
      )}
    </PageSection>
  );
};
