import * as React from 'react';

export interface IConditionsDropdownHeaderProps {
  title: string;
  children: React.ReactNode;
}
export const ConditionsDropdownHeader: React.FunctionComponent<
  IConditionsDropdownHeaderProps
> = ({ children, title }) => (
  <section className="pf-c-dropdown__group">
    <h1 className="pf-c-dropdown__group-title">{title}</h1>
    <ul>{children}</ul>
  </section>
);
