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
import './SelectedConnectionTables.css';

export interface ISelectedConnectionTablesProps {
  selectedSchemaNodesLength: number;
  i18nTablesSelected: string;
  i18nEmptyTablePreview: string;
  i18nEmptyTablePreviewTitle: string;
}

export const SelectedConnectionTables: React.FunctionComponent<ISelectedConnectionTablesProps> = props => {
  return (
    <Flex
      breakpointMods={[{ modifier: 'column', breakpoint: 'md' }]}
      className={'selected-connection-tables'}
    >
      <FlexItem className={'selected-connection-tables_headingSection'}>
        <TextContent>
          <Text
            className={'selected-connection-tables_heading_text'}
            component={TextVariants.h2}
          >
            {`${props.i18nTablesSelected} (${props.selectedSchemaNodesLength}):`}
          </Text>
        </TextContent>
      </FlexItem>
      <FlexItem className={'selected-connection-tables_contentSection'}>
        {props.selectedSchemaNodesLength === 0 ? (
          <Bullseye>
            <EmptyState variant={EmptyStateVariant.small}>
              <EmptyStateIcon icon={DatabaseIcon} />
              <Title headingLevel="h2" size="lg">
                {props.i18nEmptyTablePreviewTitle}
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
