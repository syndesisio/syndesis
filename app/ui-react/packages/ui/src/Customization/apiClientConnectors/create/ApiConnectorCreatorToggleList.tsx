import * as React from 'react';

export interface IApiClientCreatorToggleListProps {
  step: number;
  i18nSelectConnector: string;
  i18nConfigureConnector: string;
  i18nNameConnector: string;
}

export const ApiClientCreatorToggleList: React.FunctionComponent<IApiClientCreatorToggleListProps> = ({
  i18nConfigureConnector,
  i18nNameConnector,
  i18nSelectConnector,
  step,
}) => {
  return (
    <button
      aria-label="Wizard Header Toggle"
      className="pf-c-wizard__toggle"
      aria-expanded="false"
    >
      <ol className="pf-c-wizard__toggle-list">
        {step === 1 && (
          <li className="pf-c-wizard__toggle-list-item">
            <span className="pf-c-wizard__toggle-num">1</span>
            {i18nSelectConnector}
          </li>
        )}
        {step === 2 && (
          <li className="pf-c-wizard__toggle-list-item">
            <span className="pf-c-wizard__toggle-num">2</span>
            {i18nConfigureConnector}
          </li>
        )}
        {step === 3 && (
          <li className="pf-c-wizard__toggle-list-item">
            <span className="pf-c-wizard__toggle-num">3</span>
            {i18nNameConnector}
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
