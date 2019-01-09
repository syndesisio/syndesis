import * as React from 'react';
import {
  IIntegrationFlowStepProps,
  IntegrationFlowStep,
} from './IntegrationFlowStep';
import { IntegrationFlowStepDetails } from './IntegrationFlowStepDetails';

export interface IIntegrationFlowStepGenericProps
  extends IIntegrationFlowStepProps {
  i18nTitle: string;
  description: string;
}

export const IntegrationFlowStepGeneric: React.FunctionComponent<
  IIntegrationFlowStepGenericProps
> = ({
  icon,
  active = false,
  i18nTitle,
  i18nTooltip,
  showDetails,
  description,
  href,
}) => (
  <IntegrationFlowStep
    icon={icon}
    active={active}
    i18nTooltip={i18nTooltip}
    showDetails={showDetails}
    href={href}
  >
    <IntegrationFlowStepDetails active={active}>
      {({ Title, GenericDescription }) => (
        <>
          <Title>{i18nTitle}</Title>
          <GenericDescription>{description}</GenericDescription>
        </>
      )}
    </IntegrationFlowStepDetails>
  </IntegrationFlowStep>
);
