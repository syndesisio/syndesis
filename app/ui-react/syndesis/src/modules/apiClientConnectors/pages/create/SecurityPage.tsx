import * as H from '@syndesis/history';
import * as React from 'react';

import {
  ApiConnectorCreatorBreadcrumb,
  ApiConnectorCreatorBreadSteps,
  ApiConnectorCreatorFooter,
  ApiConnectorCreatorLayout,
  ApiConnectorCreatorSecurity,
  ApiConnectorCreatorSecurityForm,
  ApiConnectorCreatorToggleList,
} from '@syndesis/ui';

import { IApiSummarySoap } from '@syndesis/models';
import { useRouteData } from '@syndesis/utils';
import { Translation } from 'react-i18next';
import { PageTitle } from '../../../../shared';
import { WithLeaveConfirmation } from '../../../../shared/WithLeaveConfirmation';
import { ICreateConnectorProps } from '../../models';
import resolvers from '../../resolvers';
import routes from '../../routes';

export interface ISecurityPageRouteState {
  /**
   * `configured` property contains things such
   * as the portName, serviceName used for SOAP connectors,
   * and other configured properties.
   */
  configured?: ICreateConnectorProps;
  connectorTemplateId?: string;
  specification: IApiSummarySoap;
}

export const SecurityPage: React.FunctionComponent = () => {
  const { state, history } = useRouteData<null, ISecurityPageRouteState>();
  const { configured, connectorTemplateId, specification } = state;
  const { properties } = specification;
  const { portName, serviceName, wsdlURL } =
    specification.configuredProperties!;

  const backHref = resolvers.create.review({
    configured,
    connectorTemplateId,
    specification: specification.configuredProperties!.specification,
  });

  const defaultValues: ICreateConnectorProps = {
    authenticationType:
      configured?.authenticationType ||
      properties!.authenticationType?.defaultValue,
    authorizationEndpoint:
      configured?.authorizationEndpoint ||
      properties!.authorizationEndpoint?.defaultValue,
    passwordType:
      configured?.passwordType || properties!.passwordType?.defaultValue,
    tokenEndpoint:
      configured?.tokenEndpoint || properties!.tokenEndpoint?.defaultValue,
  };

  const dropdowns = {
    authenticationTypes: (properties!.authenticationType?.enum || []).sort(
      (a, b) => a.value!.localeCompare(b.value!)
    ),
    passwordTypes: (properties!.passwordType?.enum || []).sort((a, b) =>
      a.value!.localeCompare(b.value!)
    ),
  };

  const onNext = (values?: any) => {
    if (values.authenticationType === 'unselected') {
      throw new Error('Invalid authentication type allowed');
    }

    history.push(
      resolvers.create.save({
        configured: {
          ...values,
          portName,
          serviceName,
          wsdlURL,
        },
        connectorTemplateId,
        specification,
      })
    );
  };

  return (
    <Translation ns={['apiClientConnectors', 'shared']}>
      {(t) => (
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
              <ApiConnectorCreatorBreadcrumb
                cancelHref={resolvers.list()}
                connectorsHref={resolvers.list()}
                i18nCancel={t('shared:Cancel')}
                i18nConnectors={t('apiClientConnectors:apiConnectorsPageTitle')}
                i18nCreateConnection={t(
                  'apiClientConnectors:CreateApiConnector'
                )}
              />
              <ApiConnectorCreatorSecurityForm defaultValues={defaultValues}>
                {({ handleChange, values }) => {
                  return (
                    <ApiConnectorCreatorLayout
                      content={
                        <ApiConnectorCreatorSecurity
                          dropdowns={dropdowns}
                          handleChange={handleChange}
                          i18nAccessTokenUrl={t(
                            'apiClientConnectors:create:security:accessTokenUrl'
                          )}
                          i18nAuthenticationType={t(
                            'apiClientConnectors:create:security:authTypeLabel'
                          )}
                          i18nAuthorizationUrl={t(
                            'apiClientConnectors:create:security:authorizationUrl'
                          )}
                          i18nDescription={t(
                            'apiClientConnectors:create:security:description'
                          )}
                          i18nNoSecurity={t(
                            'apiClientConnectors:create:security:noSecurity'
                          )}
                          i18nPasswordType={t(
                            'apiClientConnectors:create:security:passwordType'
                          )}
                          i18nTimestamp={t(
                            'apiClientConnectors:create:security:timestamp'
                          )}
                          i18nTitle={t(
                            'apiClientConnectors:create:security:title'
                          )}
                          i18nUsernameTokenCreated={t(
                            'apiClientConnectors:create:security:usernameTokenCreated'
                          )}
                          i18nUsernameTokenNonce={t(
                            'apiClientConnectors:create:security:usernameTokenNonce'
                          )}
                          values={values}
                        />
                      }
                      footer={
                        <ApiConnectorCreatorFooter
                          backHref={backHref}
                          onNext={() => onNext(values)}
                          i18nBack={t('shared:Back')}
                          i18nNext={t('shared:Next')}
                          isNextLoading={false}
                          isNextDisabled={false}
                        />
                      }
                      navigation={
                        <ApiConnectorCreatorBreadSteps
                          step={3}
                          i18nDetails={t(
                            'apiClientConnectors:create:details:title'
                          )}
                          i18nReview={t(
                            'apiClientConnectors:create:review:title'
                          )}
                          i18nSecurity={t(
                            'apiClientConnectors:create:security:title'
                          )}
                          i18nSelectMethod={t(
                            'apiClientConnectors:create:selectMethod:title'
                          )}
                        />
                      }
                      toggle={
                        <ApiConnectorCreatorToggleList
                          step={1}
                          i18nDetails={t(
                            'apiClientConnectors:create:details:title'
                          )}
                          i18nReview={t(
                            'apiClientConnectors:create:review:title'
                          )}
                          i18nSecurity={t(
                            'apiClientConnectors:create:security:title'
                          )}
                          i18nSelectMethod={t(
                            'apiClientConnectors:create:selectMethod:title'
                          )}
                        />
                      }
                    />
                  );
                }}
              </ApiConnectorCreatorSecurityForm>
            </>
          )}
        </WithLeaveConfirmation>
      )}
    </Translation>
  );
};
