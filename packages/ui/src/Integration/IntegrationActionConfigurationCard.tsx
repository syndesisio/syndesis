import * as React from 'react';

export interface IIntegrationActionConfigurationFormProps {
  content: JSX.Element;
  title: string;
}

export const IntegrationActionConfigurationCard: React.FunctionComponent<
  IIntegrationActionConfigurationFormProps
> = ({ content, title }) => (
  <div className={'container-fluid'}>
    <div className="row row-cards-pf">
      <div className="card-pf">
        <div className="card-pf-title">{title}</div>
        <div className="card-pf-body">{content}</div>
      </div>
    </div>
  </div>
);
