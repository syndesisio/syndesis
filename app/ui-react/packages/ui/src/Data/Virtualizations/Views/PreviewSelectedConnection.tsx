import {
  Bullseye,
  EmptyState,
  EmptyStateBody,
  EmptyStateIcon,
  EmptyStateVariant,
  Flex,
  FlexItem,
  Text,
  TextContent,
  TextVariants,
  Title,
} from '@patternfly/react-core';
import { DatabaseIcon } from '@patternfly/react-icons';
import { SchemaNodeInfo } from '@syndesis/models';
import * as React from 'react';
import './PreviewSelectedConnection.css';

export interface IPreviewSelectedConnectionProps {
  selectedSchemaNodes: SchemaNodeInfo[];
  i18nTablesSelected: string;
  i18nEmptyTablePreview: string;
}

export const PreviewSelectedConnection: React.FunctionComponent<IPreviewSelectedConnectionProps> = props => {
  return (
    <Flex
      breakpointMods={[{ modifier: 'column', breakpoint: 'md' }]}
      className={'preview-selected-Connection'}
    >
      <FlexItem className={'preview-selected-Connection_headingSection'}>
        <TextContent>
          <Text
            className={'preview-selected-Connection_heading_text'}
            component={TextVariants.h2}
          >
            {`${props.i18nTablesSelected} (${props.selectedSchemaNodes.length}):`}
          </Text>
        </TextContent>
      </FlexItem>
      <FlexItem className={'preview-selected-Connection_contentSection'}>
        {props.selectedSchemaNodes.length === 0 ? (
          <Bullseye>
            <EmptyState variant={EmptyStateVariant.small}>
              <EmptyStateIcon icon={DatabaseIcon} />
              <Title headingLevel="h2" size="lg">
                {props.i18nTablesSelected}
              </Title>
              <EmptyStateBody>
                {props.i18nEmptyTablePreview}
              </EmptyStateBody>
            </EmptyState>
          </Bullseye>
        ) : (
          props.children
        )}
      </FlexItem>
    </Flex>
  );
};
