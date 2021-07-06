import * as React from 'react';

import { WizardNav, WizardNavItem } from '@patternfly/react-core';

export interface IApiConnectorCreatorBreadStepsProps {
  /**
   * The one-based active step number.
   */
  step: number;
  i18nConfiguration: string;
  i18nDetails: string;
  i18nReview: string;
  i18nSecurity: string;
  i18nSelectMethod: string;
}

/**
 * A component to display the PatternFly Wizard Steps specific to the integration
 * creator flow.
 * @see [step]{@link IApiConnectorCreatorBreadStepsProps#step}
 */
export const ApiConnectorCreatorBreadSteps: React.FunctionComponent<IApiConnectorCreatorBreadStepsProps> =
  ({
    i18nConfiguration,
    i18nDetails,
    i18nReview,
    i18nSecurity,
    i18nSelectMethod,
    step,
  }) => (
    <WizardNav>
      <WizardNavItem
        step={1}
        isCurrent={step === 1}
        isDisabled={step < 1}
        text={i18nSelectMethod}
      />
      <WizardNavItem
        step={2}
        isCurrent={step === 2}
        isDisabled={step < 2}
        text={i18nReview}
      />
      <WizardNavItem
        step={3}
        isCurrent={step === 3}
        isDisabled={step < 3}
        text={i18nConfiguration}
      />
      <WizardNavItem
        step={4}
        isCurrent={step === 4}
        isDisabled={step < 4}
        text={i18nSecurity}
      />
      <WizardNavItem
        step={5}
        isCurrent={step === 5}
        isDisabled={step < 5}
        text={i18nDetails}
      />
    </WizardNav>
  );
