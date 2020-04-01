import * as React from 'react';

export interface IConnectionCreatorToggleListProps {
  step: number;
  i18nSelectConnector: string;
  i18nConfigureConnection: string;
  i18nNameConnection: string;
}

export const ConnectionCreatorToggleList: React.FunctionComponent<IConnectionCreatorToggleListProps> = ({
  i18nConfigureConnection,
  i18nNameConnection,
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
            {i18nConfigureConnection}
          </li>
        )}
        {step === 3 && (
          <li className="pf-c-wizard__toggle-list-item">
            <span className="pf-c-wizard__toggle-num">3</span>
            {i18nNameConnection}
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
