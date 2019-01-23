import * as React from 'react';

export interface IIntegrationActionSelectorFormProps {
  content: JSX.Element;
  title: string;
}

export const IntegrationActionSelectorCard: React.FunctionComponent<
  IIntegrationActionSelectorFormProps
> = ({ content, title }) => (
  <div className="card-pf">
    <div className="card-pf-title">{title}</div>
    <div className="card-pf-body">{content}</div>
  </div>
);
