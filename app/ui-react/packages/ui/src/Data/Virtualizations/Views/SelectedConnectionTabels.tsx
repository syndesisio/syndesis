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
import './SelectedConnectionTabels.css';

export interface ISelectedConnectionTabelsProps {
  selectedSchemaNodesLength: number;
  i18nTablesSelected: string;
  i18nEmptyTablePreview: string;
}

export const SelectedConnectionTabels: React.FunctionComponent<ISelectedConnectionTabelsProps> = props => {
  return (
    <Flex
      breakpointMods={[{ modifier: 'column', breakpoint: 'md' }]}
      className={'selected-connection-tabels'}
    >
      <FlexItem className={'selected-connection-tabels_headingSection'}>
        <TextContent>
          <Text
            className={'selected-connection-tabels_heading_text'}
            component={TextVariants.h2}
          >
            {`${props.i18nTablesSelected} (${props.selectedSchemaNodesLength}):`}
          </Text>
        </TextContent>
      </FlexItem>
      <FlexItem className={'selected-connection-tabels_contentSection'}>
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
