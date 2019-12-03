import * as H from '@syndesis/history';
import {
  ButtonLink,
  UnrecoverableError,
  UnrecoverableErrorIcon,
} from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';

export interface IActionDescriptorFetchError {
  connectionDetailHref: H.LocationDescriptor;
  error: Error | string;
  errorInfo?: React.ErrorInfo;
}

export const ActionDescriptorFetchError: React.FC<
  IActionDescriptorFetchError
> = ({ connectionDetailHref, error, errorInfo }) => (
  <Translation ns={['shared', 'integrations']}>
    {t => (
      <UnrecoverableError
        i18nTitle={t(
          'integrations:editor.endpoint.configureAction.descriptorFetchError.title'
        )}
        i18nInfo={t(
          'integrations:editor.endpoint.configureAction.descriptorFetchError.info'
        )}
        i18nShowErrorInfoLabel={t('error.showErrorInfoButton')}
        icon={UnrecoverableErrorIcon.WARNING}
        actions={
          <ButtonLink as={'primary'} href={connectionDetailHref}>
            {t(
              'integrations:editor.endpoint.configureAction.descriptorFetchError.buttonLabel'
            )}
          </ButtonLink>
        }
        secondaryActions={false}
        error={typeof error === 'string' ? Error(error) : error}
        errorInfo={errorInfo}
      />
    )}
  </Translation>
);
