import { Text, TextContent, TextVariants } from '@patternfly/react-core';
import * as React from 'react';
import { IFormControlProps } from '../models';
import './FormLegendComponent.css';
export const FormLegendComponent: React.FunctionComponent<
  IFormControlProps
> = props => (
  <TextContent className={'pf-c-form__group auto-form-legend'}>
    <Text component={TextVariants.h4}>{props.property.displayName}</Text>
  </TextContent>
);
