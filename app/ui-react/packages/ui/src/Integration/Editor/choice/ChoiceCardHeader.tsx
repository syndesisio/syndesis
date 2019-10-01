import { Title } from '@patternfly/react-core';
import * as React from 'react';

export interface IChoiceCardHeaderProps {
  i18nConditions: string;
}

export const ChoiceCardHeader: React.FunctionComponent<
  IChoiceCardHeaderProps
> = ({ i18nConditions }) => <Title className="syn-card__title" headingLevel="h2" size="md">{i18nConditions}</Title>;
