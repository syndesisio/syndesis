import {
  DataList,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  EmptyStateVariant,
  Text,
  Title,
  Tooltip
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

export const ExtensionListView: React.FunctionComponent<
  IExtensionListViewProps
> = props => {
  return (
    <PageSection>
      <ListViewToolbar {...props}>
        <div className={'form-group'}>
          <ButtonLink data-testid={'extension-list-view-import-button'}
                      href={props.linkImportExtension}
                      as={'primary'}>
            {props.i18nLinkImportExtension}
          </ButtonLink>
        </div>
      </ListViewToolbar>
      {props.i18nTitle !== '' && <Title size={'lg'}>{props.i18nTitle}</Title>}
      {props.i18nDescription !== '' && (
        <Text dangerouslySetInnerHTML={{ __html: props.i18nDescription }} />
      )}
      {props.children ? (
        <DataList aria-label={'extension list'}>{props.children}</DataList>
      ) : (
        <EmptyState variant={EmptyStateVariant.full}>
          <EmptyStateIcon icon={AddCircleOIcon}/>
          <Title headingLevel={'h5'} size={'lg'}>
            {props.i18nEmptyStateTitle}
          </Title>
          <EmptyStateBody>{props.i18nEmptyStateInfo}</EmptyStateBody>
          <Tooltip
            position={'top'}
            content={
              props.i18nLinkImportExtensionTip
                ? props.i18nLinkImportExtensionTip
                : props.i18nLinkImportExtension
            }
            enableFlip={true}
          >
            <>
              <br/>
              <ButtonLink data-testid={'extension-list-view-empty-state-import-button'}
                          href={props.linkImportExtension}
                          as={'primary'}>
                {props.i18nLinkImportExtension}
              </ButtonLink>
            </>
          </Tooltip>
        </EmptyState>
      )}
    </PageSection>
  );
};
