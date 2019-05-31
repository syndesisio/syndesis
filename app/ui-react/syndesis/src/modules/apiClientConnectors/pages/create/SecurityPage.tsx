import * as H from '@syndesis/history';
import { APISummary } from '@syndesis/models';
import {
  ApiClientConnectorCreateSecurity,
  ApiConnectorCreatorLayout,
  ButtonLink,
  IAuthenticationType,
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

  const authenticationType: IAuthenticationType[] = [
    {
      label: 'HTTP Basic Authentication',
      value: 'basic',
    },
    {
      label: 'OAuth 2.0',
      value: 'oauth2',
    },
  ];

  const connectorExample = {
    properties: {
      authenticationType: {
        description: 'Type of authentication used to connect to the API',
        enum: authenticationType,
      },
      authorizationEndpoint: {
        defaultValue: '/',
        description: 'URL for the start of the OAuth flow',
        displayName: 'OAuth Authorization Endpoint URL',
      },
      tokenEndpoint: {
        defaultValue: '/',
      },
    },
  };

  const onNext = (
    accessToken?: string,
    authType?: IAuthenticationType[],
    authUrl?: string
  ) => {
    history.push(resolvers.create.save(state));
    // Leaving the following just so lint doesn't complain
    return { accessToken, authType, authUrl };
  };

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
                        connectorExample.properties.tokenEndpoint.defaultValue
                      }
                      authenticationType={
                        connectorExample.properties.authenticationType.enum
                      }
                      authorizationUrl={
                        connectorExample.properties.authorizationEndpoint
                          .defaultValue
                      }
                      i18nAccessTokenUrl={t(
                        'apiClientConnectors:create:security:accessTokenUrl'
                      )}
                      i18nAuthorizationUrl={t(
                        'apiClientConnectors:create:security:authorizationUrl'
                      )}
                      i18nBtnNext={t('Next')}
                      i18nNoSecurity={t(
                        'apiClientConnectors:create:security:noSecurity'
                      )}
                      i18nTitle={t('apiClientConnectors:create:security:title')}
                      onNext={onNext}
                    />
                    <div>
                      <ButtonLink
                        href={resolvers.create.review({
                          specification: state.specification
                            .configuredProperties!.specification,
                        })}
                      >
                        {t('Back')}
                      </ButtonLink>
                      &nbsp;
                      <ButtonLink onClick={onNext} as={'primary'}>
                        {t('Next')}
                      </ButtonLink>
                    </div>
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
