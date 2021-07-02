import { WizardNav, WizardNavItem } from '@patternfly/react-core';
import * as React from 'react';

export interface IConnectionCreatorStepsProps {
  /**
   * The one-based active step number.
   */
  step: number;
  i18nSelectConnector: string;
  i18nConfigureConnection: string;
  i18nNameConnection: string;
}

/**
 * A component to display the PatternFly Wizard Steps specific to the integration
 * creator flow.
 * @see [step]{@link IConnectionCreatorStepsProps#step}
 */
export const ConnectionCreatorBreadSteps: React.FunctionComponent<IConnectionCreatorStepsProps> =
  ({
    i18nConfigureConnection,
    i18nNameConnection,
    i18nSelectConnector,
    step,
  }) => (
    <WizardNav>
      <WizardNavItem
        step={1}
        isCurrent={step === 1}
        isDisabled={step < 1}
        content={i18nSelectConnector}
      />
      <WizardNavItem
        step={2}
        isCurrent={step === 2}
        isDisabled={step < 2}
        content={i18nConfigureConnection}
      />
      <WizardNavItem
        step={3}
        isCurrent={step === 3}
        isDisabled={step < 3}
        content={i18nNameConnection}
      />
    </WizardNav>
  );
