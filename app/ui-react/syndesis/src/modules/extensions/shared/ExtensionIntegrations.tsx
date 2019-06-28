import { useExtensionIntegrations } from '@syndesis/api';
import {
  ExtensionIntegrationsTable,
  IExtensionIntegration,
  Loader,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import i18n from '../../../i18n';
import { ApiError } from '../../../shared';

export interface IExtensionIntegrationsProps {
  extensionId: string;
  uses: number;
  onSelectIntegration: (integrationId: string) => void;
}

/**
 * A function component that lists the integrations that uses a specific extension.
 */
export const ExtensionIntegrations: React.FunctionComponent<
  IExtensionIntegrationsProps
> = props => {
  const getUsageMessage = (uses: number): string => {
    if (uses === 1) {
      return i18n.t('extensions:usedByOne');
    }

    return i18n.t('extensions:usedByMulti', {
      count: uses,
    });
  };

  const { resource: integrations, hasData, error } = useExtensionIntegrations(
    props.extensionId
  );

  return (
    <WithLoader
      error={error !== false}
      loading={!hasData}
      loaderChildren={<Loader />}
      errorChildren={<ApiError error={error as Error} />}
    >
      {() => (
        <Translation ns={['extensions', 'shared']}>
          {t => (
            <ExtensionIntegrationsTable
              i18nDescription={t('shared:Description')}
              i18nName={t('shared:Name')}
              i18nUsageMessage={getUsageMessage(props.uses)}
              onSelectIntegration={props.onSelectIntegration}
              data={integrations as IExtensionIntegration[]}
            />
          )}
        </Translation>
      )}
    </WithLoader>
  );
};
