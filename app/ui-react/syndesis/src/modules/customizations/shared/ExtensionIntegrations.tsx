import { WithExtensionIntegrations } from '@syndesis/api';
import {
  ExtensionIntegrationsTable,
  IExtensionIntegration,
  Loader,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import i18n from '../../../i18n';

export interface IExtensionIntegrationsProps {
  extensionId: string;
  uses: number;
  onSelectIntegration: (integrationId: string) => void;
}

/**
 * A component that lists the integrations that uses a specific extension.
 */
export default class ExtensionIntegrations extends React.Component<
  IExtensionIntegrationsProps
> {
  public getUsageMessage(uses: number): string {
    if (uses === 1) {
      return i18n.t('customizations:usedByOne');
    }

    return i18n.t('customizations:usedByMulti', {
      count: uses,
    });
  }

  public render() {
    return (
      <WithExtensionIntegrations extensionId={this.props.extensionId}>
        {({ data, hasData, error }) => (
          <WithLoader
            error={error}
            loading={!hasData}
            loaderChildren={<Loader />}
            errorChildren={<div>TODO</div>}
          >
            {() => (
              <Translation ns={['customizations', 'shared']}>
                {t => (
                  <ExtensionIntegrationsTable
                    i18nDescription={t('shared:Description')}
                    i18nName={t('shared:Name')}
                    i18nUsageMessage={this.getUsageMessage(this.props.uses)}
                    onSelectIntegration={this.props.onSelectIntegration}
                    data={data as IExtensionIntegration[]}
                  />
                )}
              </Translation>
            )}
          </WithLoader>
        )}
      </WithExtensionIntegrations>
    );
  }
}
