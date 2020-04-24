import * as React from 'react';

export interface IApiConnectorCreatorToggleListProps {
  step: number;
  i18nDetails: string;
  i18nReview: string;
  i18nSecurity: string;
  i18nSelectMethod: string;
}

export const ApiConnectorCreatorToggleList: React.FunctionComponent<IApiConnectorCreatorToggleListProps> = (
  {
    i18nDetails,
    i18nReview,
    i18nSecurity,
    i18nSelectMethod,
    step,
  }) => {
  return (
    <button
      aria-label="Wizard Navigation Toggle"
      className="pf-c-wizard__toggle"
      aria-expanded="false"
    >
      <ol className="pf-c-wizard__toggle-list">
        {step === 1 && (
          <li className="pf-c-wizard__toggle-list-item">
            <span className="pf-c-wizard__toggle-num">1</span>
            {i18nSelectMethod}
          </li>
        )}
        {step === 2 && (
          <li className="pf-c-wizard__toggle-list-item">
            <span className="pf-c-wizard__toggle-num">2</span>
            {i18nReview}
          </li>
        )}
        {step === 3 && (
          <li className="pf-c-wizard__toggle-list-item">
            <span className="pf-c-wizard__toggle-num">3</span>
            {i18nSecurity}
          </li>
        )}
        {step === 4 && (
          <li className="pf-c-wizard__toggle-list-item">
            <span className="pf-c-wizard__toggle-num">4</span>
            {i18nDetails}
          </li>
        )}
      </ol>
      <i
        style={{ display: 'none' }}
        className="fas fa-caret-down pf-c-wizard__toggle-icon"
        aria-hidden="true"
      />
    </button>
  );
};
