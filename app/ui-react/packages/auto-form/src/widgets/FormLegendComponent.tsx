import { Text, TextContent, TextVariants } from '@patternfly/react-core';
import * as React from 'react';
import { IFormControlProps } from '../models';

export const FormLegendComponent: React.FunctionComponent<
  IFormControlProps
> = props => (
  <TextContent>
    <Text component={TextVariants.h4}>{props.property.displayName}</Text>
  </TextContent>
);
