import {
  EmptyState,
  EmptyStateBody,
  EmptyStateVariant,
  Flex,
  FlexItem,
  Title,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import { ListView } from 'patternfly-react';
import * as React from 'react';
import { ButtonLink } from '../../../Layout';
import './ConnectionSchemaList.css';
import { ConnectionSchemaListSkeleton } from './ConnectionSchemaListSkeleton';

export interface IConnectionSchemaListProps {
  hasListData: boolean;
  i18nEmptyStateInfo: string;
  i18nEmptyStateTitle: string;
  i18nLinkCreateConnection: string;
  linkToConnectionCreate: H.LocationDescriptor;
  loading: boolean;
}

export const ConnectionSchemaList: React.FunctionComponent<IConnectionSchemaListProps> = props => {
  return (
    <>
      {props.loading ? (
        <ListView>
          <ConnectionSchemaListSkeleton width={800} />
        </ListView>
      ) : props.hasListData ? (
        <Flex
          breakpointMods={[{ modifier: 'column', breakpoint: 'md' }]}
          className={'connection-schema-list_content'}
        >
          <FlexItem className={'connection-schema-list_headingSection'}>
            <Flex>
              {/* <FlexItem>
                <TextContent>
                  <Text className={'connection-schema-list_heading_text'} component={TextVariants.h2}>
                    patternfly 4 filter |
                  </Text>
                </TextContent>
              </FlexItem> */}
              <FlexItem breakpointMods={[{ modifier: 'align-right', breakpoint: 'md' }]}>
                <ButtonLink
                  className={'connection-schema-list-create-connection-button'}
                  data-testid={
                    'dv-connection-schema-list-create-connection-button'
                  }
                  href={props.linkToConnectionCreate}
                  as={'default'}
                >
                  {props.i18nLinkCreateConnection}
                </ButtonLink>
              </FlexItem>
            </Flex>
          </FlexItem>
          <FlexItem className={'connection-schema-list_contentSection'}>
            <div className={'connection-schema-list'}>
              <ListView>{props.children}</ListView>
            </div>
          </FlexItem>
        </Flex>
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
    </>
  );
};
