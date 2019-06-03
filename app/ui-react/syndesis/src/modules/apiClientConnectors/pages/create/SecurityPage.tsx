import * as H from '@syndesis/history';
import { APISummary } from '@syndesis/models';
import {
  ApiClientConnectorCreateSecurity,
  ApiConnectorCreatorLayout,
  PageSection,
} from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { PageTitle } from '../../../../shared';
import { WithLeaveConfirmation } from '../../../../shared/WithLeaveConfirmation';
import {
  ApiConnectorCreatorBreadcrumb,
  ApiConnectorCreatorWizardSteps,
} from '../../components';
import resolvers from '../../resolvers';
import routes from '../../routes';

export interface ISecurityPageRouteState {
  specification: APISummary;
}

export const SecurityPage: React.FunctionComponent = () => {
  const { state, history } = useRouteData<null, ISecurityPageRouteState>();
  const backHref = resolvers.create.review({
    specification: state.specification.configuredProperties!.specification,
  });

  const onNext = (
    accessToken?: string,
    authType?: string | undefined,
    authUrl?: string
  ) => {
    console.log(JSON.stringify(state));
    history.push(
      resolvers.create.save({
        authenticationType: authType,
        authorizationEndpoint: authUrl,
        specification: state.specification,
        tokenEndpoint: accessToken,
      })
    );
  };

  console.log('state: ' + JSON.stringify(state));

  return (
    <Translation ns={['apiClientConnectors', 'shared']}>
      {t => (
        <WithLeaveConfirmation
          i18nTitle={t('apiClientConnectors:create:unsavedChangesTitle')}
          i18nConfirmationMessage={t(
            'apiClientConnectors:create:unsavedChangesMessage'
          )}
          shouldDisplayDialog={(location: H.LocationDescriptor) => {
            const url =
              typeof location === 'string' ? location : location.pathname!;
            return !url.startsWith(routes.create.root);
          }}
        >
          {() => (
            <>
              <PageTitle
                title={t('apiClientConnectors:create:security:title')}
              />
              <ApiConnectorCreatorBreadcrumb cancelHref={resolvers.list()} />
              <ApiConnectorCreatorLayout
                header={<ApiConnectorCreatorWizardSteps step={3} />}
                content={
                  <PageSection>
                    <ApiClientConnectorCreateSecurity
                      accessToken={
                        state.specification.properties!.tokenEndpoint
                          .defaultValue
                      }
                      authenticationTypes={
                        state.specification.properties!.authenticationType.enum
                      }
                      authorizationUrl={
                        state.specification.properties!.authorizationEndpoint
                          .defaultValue
                      }
                      backHref={backHref}
                      i18nAccessTokenUrl={t(
                        'apiClientConnectors:create:security:accessTokenUrl'
                      )}
                      i18nAuthorizationUrl={t(
                        'apiClientConnectors:create:security:authorizationUrl'
                      )}
                      i18nBtnBack={t('Back')}
                      i18nBtnNext={t('Next')}
                      i18nNoSecurity={t(
                        'apiClientConnectors:create:security:noSecurity'
                      )}
                      i18nTitle={t('apiClientConnectors:create:security:title')}
                      onNext={onNext}
                    />
                  </PageSection>
                }
              />
            </>
          )}
        </WithLeaveConfirmation>
      )}
    </Translation>
  );
};
