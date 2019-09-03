import * as H from '@syndesis/history';
import { ChoiceConfigurationView } from '@syndesis/ui';
import * as React from 'react';
import { IUIStep } from '../interfaces';
import { createChoiceConfiguration, getConditionExpression } from './utils';

export interface IChoiceStepExpanderBodyProps {
  step: IUIStep;
  getFlowHref(flowId: string): H.LocationDescriptor;
}
export const ChoiceStepExpanderBody: React.FunctionComponent<
  IChoiceStepExpanderBodyProps
> = ({ step, getFlowHref }) => {
  // parse the configured properties
  const configuration = createChoiceConfiguration(
    step.configuredProperties || {}
  );
  // create links
  const flowItems = configuration.flows.map(flowOption => ({
    condition: getConditionExpression(flowOption),
    href: getFlowHref(flowOption.flow),
  }));
  const defaultFlowHref = configuration.defaultFlowEnabled
    ? getFlowHref(configuration.defaultFlow!)
    : undefined;

  return (
    <ChoiceConfigurationView
      flowItems={flowItems}
      useDefaultFlow={configuration.defaultFlowEnabled}
      defaultFlowHref={defaultFlowHref}
      i18nWhen={'When'}
      i18nOtherwise={'Otherwise'}
      i18nOpenFlow={'Open Flow'}
      i18nUseDefaultFlow={'Use a default flow'}
    />
  );
};
