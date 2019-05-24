import { Text, TextContent, Title, TitleLevel } from '@patternfly/react-core';
import * as React from 'react';

export const IntegrationEditorStepsAdderHeader: React.FunctionComponent = () => (
  <TextContent>
    <Title size={'2xl'} headingLevel={TitleLevel.h1}>
      Add to Integration
    </Title>
    <Text>
      You can continue adding steps and connections to your integration as well.
    </Text>
  </TextContent>
);
