// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import * as React from 'react';
import {
  IIntegrationFlowStepProps,
  IntegrationFlowStep,
} from './IntegrationFlowStep';
import { IntegrationFlowStepDetails } from './IntegrationFlowStepDetails';

export interface IIntegrationFlowStepWithOverviewProps
  extends IIntegrationFlowStepProps {
  /**
   * The title of the extended information table.
   */
  i18nTitle: string;
  /**
   * The name of the connection used for the step.
   */
  name: string;
  /**
   * The name of the action used for the step.
   */
  action?: string;
  /**
   * The data-type set up for the step.
   */
  dataType?: string;
}

/**
 * A component to show an already configured step in the sidebar of the
 * integration editor.
 *
 * @see [i18nTitle]{@link IIntegrationFlowStepWithOverviewProps#i18nTitle}
 * @see [i18nTooltip]{@link IIntegrationFlowStepProps#i18nTooltip}
 * @see [icon]{@link IIntegrationFlowStepProps#icon}
 * @see [active]{@link IIntegrationFlowStepProps#active}
 * @see [showDetails]{@link IIntegrationFlowStepProps#showDetails}
 * @see [name]{@link IIntegrationFlowStepWithOverviewProps#name}
 * @see [action]{@link IIntegrationFlowStepWithOverviewProps#action}
 * @see [dataType]{@link IIntegrationFlowStepWithOverviewProps#dataType}
 * @constructor
 */
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
}) => (
  <IntegrationFlowStep
    icon={icon}
    active={active}
    i18nTooltip={i18nTooltip}
    showDetails={showDetails}
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
