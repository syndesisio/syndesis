// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import * as React from 'react';

export interface IIntegrationActionSelectorCardProps {
  content: JSX.Element;
  title: string;
}

export const IntegrationActionSelectorCard: React.FunctionComponent<
  IIntegrationActionSelectorCardProps
> = ({ content, title }) => (
  <div className="card-pf">
    <div className="card-pf-title">{title}</div>
    <div className="card-pf-body">{content}</div>
  </div>
);
