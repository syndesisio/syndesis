import { Action, ActionDescriptor } from '@syndesis/models';
import { IntegrationEditorNothingToConfigure } from '@syndesis/ui';
import * as React from 'react';
import i18n from '../../../../../i18n';
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
      i18nAlert={i18n.t('integrations:editor:endpoint:configureAction:noProperties')}
      i18nBackAction={i18n.t('integrations:editor:endpoint:configureAction:chooseAction')}
      i18nNext={i18n.t('shared:Next')}
      submitForm={submitForm}
      backActionHref={chooseActionHref}
    />
  );
};
