import { Action, ActionDescriptor } from '@syndesis/models';
import { IntegrationEditorNothingToConfigure } from '@syndesis/ui';
import * as React from 'react';
import { IWithConfigurationFormProps } from './WithConfigurationForm';

export interface INothingToConfigureProps
  extends Pick<IWithConfigurationFormProps, 'onUpdatedIntegration'>,
    Pick<IWithConfigurationFormProps, 'chooseActionHref'> {
  action: Action;
  descriptor: ActionDescriptor;
}

export const NothingToConfigure: React.FunctionComponent<
  INothingToConfigureProps
> = ({ action, descriptor, chooseActionHref, onUpdatedIntegration }) => {
  const submitForm = () => {
    onUpdatedIntegration({
      action: { ...action, descriptor },
      moreConfigurationSteps: false,
      values: null,
    });
  };
  return (
    <IntegrationEditorNothingToConfigure
      i18nAlert={'There are no properties to configure for this action.'}
      i18nBackAction={'Choose Action'}
      i18nNext={'Next'}
      submitForm={submitForm}
      backActionHref={chooseActionHref}
    />
  );
};
