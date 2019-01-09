import * as React from 'react';
import {
  IIntegrationFlowStepProps,
  IntegrationFlowStep,
} from './IntegrationFlowStep';
import { IntegrationFlowStepDetails } from './IntegrationFlowStepDetails';

export interface IIntegrationFlowStepWithOverviewProps
  extends IIntegrationFlowStepProps {
  i18nTitle: string;
  name: string;
  action: string;
  dataType: string;
}

export const IntegrationFlowStepWithOverview: React.FunctionComponent<
  IIntegrationFlowStepWithOverviewProps
> = ({
  i18nTitle,
  i18nTooltip,
  icon,
  active = false,
  showDetails,
  name,
  action,
  dataType,
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
      {({ Title, StepOverview }) => (
        <>
          <Title>{i18nTitle}</Title>
          <StepOverview
            nameI18nLabel={'Name:'}
            name={name}
            actionI18nLabel={'Action:'}
            action={action}
            dataTypeI18nLabel={'Data Type:'}
            dataType={dataType}
          />
        </>
      )}
    </IntegrationFlowStepDetails>
  </IntegrationFlowStep>
);
