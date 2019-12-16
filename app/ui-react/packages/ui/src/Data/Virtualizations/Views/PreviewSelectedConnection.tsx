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
import * as React from 'react';
import './PreviewSelectedConnection.css';

export interface IPreviewSelectedConnectionProps {
  selectedSchemaNodesLength: number;
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
            {`${props.i18nTablesSelected} (${props.selectedSchemaNodesLength}):`}
          </Text>
        </TextContent>
      </FlexItem>
      <FlexItem className={'preview-selected-Connection_contentSection'}>
        {props.selectedSchemaNodesLength === 0 ? (
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
