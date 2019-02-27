// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import * as React from 'react';
import {
  IIntegrationFlowStepProps,
  IntegrationFlowStep,
} from './IntegrationFlowStep';
import { IntegrationFlowStepDetails } from './IntegrationFlowStepDetails';

export interface IIntegrationFlowStepGenericProps
  extends IIntegrationFlowStepProps {
  /**
   * The title of the extended information table.
   */
  i18nTitle: string;
  /**
   * The content of the extended information table.
   */
  description: string;
}

/**
 * A component to show an unconfigured (or being configured) step in the
 * sidebar of the integration editor.
 *
 * @see [icon]{@link IIntegrationFlowStepProps#icon}
 * @see [active]{@link IIntegrationFlowStepProps#active}
 * @see [i18nTitle]{@link IIntegrationFlowStepGenericProps#i18nTitle}
 * @see [i18nTooltip]{@link IIntegrationFlowStepProps#i18nTooltip}
 * @see [showDetails]{@link IIntegrationFlowStepProps#showDetails}
 * @see [description]{@link IIntegrationFlowStepGenericProps#description}
 * @see [href]{@link IIntegrationFlowStepProps#href}
 */
export const IntegrationFlowStepGeneric: React.FunctionComponent<
  IIntegrationFlowStepGenericProps
> = ({
  icon,
  active = false,
  i18nTitle,
  i18nTooltip,
  showDetails,
  description,
}) => (
  <IntegrationFlowStep
    icon={icon}
    active={active}
    i18nTooltip={i18nTooltip}
    showDetails={showDetails}
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
