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
}

export const PreviewSelectedConnection: React.FunctionComponent<
  IPreviewSelectedConnectionProps
> = props => {
  return (
    <Flex
      breakpointMods={[{ modifier: 'column', breakpoint: 'md' }]}
      className={'preview-selected-Connection'}
    >
      <FlexItem className={'preview-selected-Connection_headingSection'}>
        <TextContent>
          <Text className={'preview-selected-Connection_heading_text'} component={TextVariants.h2}>
            {`Tables selected (${props.selectedSchemaNodes.length}):`}
          </Text>
        </TextContent>
      </FlexItem>
      <FlexItem className={'preview-selected-Connection_contentSection'}>
        {props.selectedSchemaNodes.length === 0 ? (
          <Bullseye>
            <EmptyState variant={EmptyStateVariant.small}>
              <EmptyStateIcon icon={DatabaseIcon} />
              <Title headingLevel="h2" size="lg">
                Tables selected
              </Title>
              <EmptyStateBody>
                please select a one or more tables taht you want to bring to the
                view
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
