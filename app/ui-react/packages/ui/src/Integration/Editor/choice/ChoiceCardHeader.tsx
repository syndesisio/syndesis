import * as React from 'react';

export interface IChoiceCardHeaderProps {
  i18nConditions: string;
}

export const ChoiceCardHeader: React.FunctionComponent<
  IChoiceCardHeaderProps
> = ({ i18nConditions }) => <h2 className="card-pf-title">{i18nConditions}</h2>;
