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
import * as React from 'react';
import { ButtonLink, PageSection } from '../../Layout';
import { IListViewToolbarProps, ListViewToolbar } from '../../Shared';

export interface IApiConnectorListViewProps extends IListViewToolbarProps {
  i18nDescription: string;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nLinkCreateApiConnector: string;
  i18nLinkCreateApiConnectorTip?: string;
  i18nName: string;
  i18nTitle: string;
  linkCreateApiConnector: string;
}

export const ApiConnectorListView: React.FunctionComponent<IApiConnectorListViewProps> =
  ({
    children,
    i18nDescription,
    i18nEmptyStateInfo,
    i18nEmptyStateTitle,
    i18nLinkCreateApiConnector,
    i18nLinkCreateApiConnectorTip,
    i18nName,
    i18nTitle,
    linkCreateApiConnector,
    ...rest
  }) => {
    return (
      <PageSection>
        <ListViewToolbar {...rest}>
          <div className={'form-group'}>
            <ButtonLink
              data-testid={'api-connector-list-view-create-button'}
              href={linkCreateApiConnector}
              as={'primary'}
            >
              {i18nLinkCreateApiConnector}
            </ButtonLink>
          </div>
        </ListViewToolbar>
        {i18nTitle !== '' && (
          <Title size="xl" headingLevel={'h2'}>
            {i18nTitle}
          </Title>
        )}
        {i18nDescription !== '' && (
          <Text dangerouslySetInnerHTML={{ __html: i18nDescription }} />
        )}
        {children ? (
          <DataList
            data-testid={'api-connector-list'}
            aria-label={'api connector list'}
          >
            {children}
          </DataList>
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
                i18nLinkCreateApiConnectorTip
                  ? i18nLinkCreateApiConnectorTip
                  : i18nLinkCreateApiConnector
              }
              enableFlip={true}
            >
              <>
                <br />
                <ButtonLink
                  data-testid={
                    'api-connector-list-view-empty-state-create-button'
                  }
                  href={linkCreateApiConnector}
                  as={'primary'}
                >
                  {i18nLinkCreateApiConnector}
                </ButtonLink>
              </>
            </Tooltip>
          </EmptyState>
        )}
      </PageSection>
    );
  };
