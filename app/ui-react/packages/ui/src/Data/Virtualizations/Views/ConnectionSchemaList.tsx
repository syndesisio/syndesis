import {
  EmptyState,
  EmptyStateBody,
  EmptyStateVariant,
  Title,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import { ListView } from 'patternfly-react';
import * as React from 'react';
import { ButtonLink, PageSection } from '../../../Layout';
import './ConnectionSchemaList.css';

export interface IConnectionSchemaListProps {
  hasListData: boolean;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nLinkCreateConnection: string;
  linkToConnectionCreate: H.LocationDescriptor;
}

export const ConnectionSchemaList: React.FunctionComponent<
  IConnectionSchemaListProps
> = props => {
  return (
    <PageSection noPadding={true} className={'connection-schema-list'}>
      {props.hasListData ? (
        <>
          <ButtonLink
            className={'connection-schema-list-create-connection-button'}
            data-testid={'dv-connection-schema-list-create-connection-button'}
            href={props.linkToConnectionCreate}
            as={'primary'}
          >
            {props.i18nLinkCreateConnection}
          </ButtonLink>
          <ListView>{props.children}</ListView>
        </>
      ) : (
        <EmptyState variant={EmptyStateVariant.full}>
          <Title headingLevel="h5" size="lg">
            {props.i18nEmptyStateTitle}
          </Title>
          <EmptyStateBody>{props.i18nEmptyStateInfo}</EmptyStateBody>
          <ButtonLink
            className={'connection-schema-list-empty-create-connection-button'}
            data-testid={
              'dv-connection-schema-list-empty-create-connection-button'
            }
            href={props.linkToConnectionCreate}
            as={'primary'}
          >
            {props.i18nLinkCreateConnection}
          </ButtonLink>
        </EmptyState>
      )}
    </PageSection>
  );
};
