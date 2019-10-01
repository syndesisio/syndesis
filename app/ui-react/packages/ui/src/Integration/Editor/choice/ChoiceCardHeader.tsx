import { Title } from '@patternfly/react-core';
import * as React from 'react';

export interface IChoiceCardHeaderProps {
  i18nConditions: string;
}

export const ChoiceCardHeader: React.FunctionComponent<
  IChoiceCardHeaderProps
> = ({ i18nConditions }) => <Title headingLevel="h2" size="lg">{i18nConditions}</Title>;
