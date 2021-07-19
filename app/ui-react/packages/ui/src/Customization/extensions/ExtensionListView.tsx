import {
  DataList,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  EmptyStateVariant,
  Text,
  Title,
  Tooltip,
} from '@patternfly/react-core';
import { AddCircleOIcon } from '@patternfly/react-icons';
import * as H from '@syndesis/history';
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

export const ExtensionListView: React.FunctionComponent<IExtensionListViewProps> =
  ({
    children,
    i18nDescription,
    i18nEmptyStateInfo,
    i18nEmptyStateTitle,
    i18nLinkImportExtension,
    i18nLinkImportExtensionTip,
    i18nName,
    i18nNameFilterPlaceholder,
    i18nTitle,
    linkImportExtension,
    ...rest
  }) => {
    return (
      <PageSection>
        <ListViewToolbar {...rest}>
          <div className={'form-group'}>
            <ButtonLink
              data-testid={'extension-list-view-import-button'}
              href={linkImportExtension}
              as={'primary'}
            >
              {i18nLinkImportExtension}
            </ButtonLink>
          </div>
        </ListViewToolbar>
        {i18nTitle !== '' && (
          <Title size={'lg'} headingLevel={'h1'}>
            {i18nTitle}
          </Title>
        )}
        {i18nDescription !== '' && (
          <Text dangerouslySetInnerHTML={{ __html: i18nDescription }} />
        )}
        {children ? (
          <DataList aria-label={'extension list'}>{children}</DataList>
        ) : (
          <EmptyState variant={EmptyStateVariant.full}>
            <EmptyStateIcon icon={AddCircleOIcon} />
            <Title headingLevel={'h5'} size={'lg'}>
              {i18nEmptyStateTitle}
            </Title>
            <EmptyStateBody>{i18nEmptyStateInfo}</EmptyStateBody>
            <Tooltip
              position={'top'}
              content={
                i18nLinkImportExtensionTip
                  ? i18nLinkImportExtensionTip
                  : i18nLinkImportExtension
              }
              enableFlip={true}
            >
              <>
                <br />
                <ButtonLink
                  data-testid={'extension-list-view-empty-state-import-button'}
                  href={linkImportExtension}
                  as={'primary'}
                >
                  {i18nLinkImportExtension}
                </ButtonLink>
              </>
            </Tooltip>
          </EmptyState>
        )}
      </PageSection>
    );
  };
