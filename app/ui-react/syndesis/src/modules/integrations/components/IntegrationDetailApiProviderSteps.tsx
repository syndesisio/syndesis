import {
  ApiProviderIcon,
  IntegrationStepsHorizontalItem,
  IntegrationStepsHorizontalView,
  MultiFlowIcon,
  PageSection,
} from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';
import './IntegrationDetailSteps.css';

export interface IIntegrationDetailApiProviderStepsProps {
  flowCount: number;
}

export const IntegrationDetailApiProviderSteps: React.FunctionComponent<
  IIntegrationDetailApiProviderStepsProps
> = props => (
  <Translation ns={['integrations', 'shared']}>
    {t => {
      const flowCountString = t('flowCount', { flowCount: props.flowCount });
      return (
        <PageSection className="integration-detail-steps">
          <IntegrationStepsHorizontalView>
            <IntegrationStepsHorizontalItem
              key={'start'}
              name={t('APIProvider')}
              title={t('APIProvider')}
              icon={<ApiProviderIcon alt={t('APIProvider')} />}
              isLast={false}
            />
            <IntegrationStepsHorizontalItem
              key={'end'}
              name={flowCountString}
              title={flowCountString}
              icon={<MultiFlowIcon alt={flowCountString} />}
              isLast={true}
            />
          </IntegrationStepsHorizontalView>
        </PageSection>
      );
    }}
  </Translation>
);
