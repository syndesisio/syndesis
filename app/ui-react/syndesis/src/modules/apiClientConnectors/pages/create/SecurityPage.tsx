import * as H from '@syndesis/history';
import { IApiSummarySoap, ICreateConnectorProps } from '@syndesis/models';
import {
  ApiConnectorCreatorBreadcrumb,
  ApiConnectorCreatorBreadSteps,
  ApiConnectorCreatorFooter,
  ApiConnectorCreatorLayout,
  ApiConnectorCreatorSecurity,
  ApiConnectorCreatorSecurityForm,
  ApiConnectorCreatorToggleList,
} from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { PageTitle } from '../../../../shared';
import { WithLeaveConfirmation } from '../../../../shared/WithLeaveConfirmation';
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
  const { connectorTemplateId, specification } = state;
  const { properties } = specification;
  const { portName, serviceName } = specification.configuredProperties!;

  const backHref = resolvers.create.review({
    configured: { portName, serviceName },
    connectorTemplateId,
    specification: specification.configuredProperties!.specification,
  });

  const defaultValues = {
    accessTokenUrl:
      properties!.tokenEndpoint && properties!.tokenEndpoint.defaultValue,
    authenticationType: properties!.authenticationType.defaultValue,
    authorizationUrl:
      properties!.authorizationEndpoint &&
      properties!.authorizationEndpoint.defaultValue,
    passwordType:
      properties!.passwordType && properties!.passwordType.defaultValue,
  };

  const dropdowns = {
    authenticationTypes:
      properties!.authenticationType &&
      (properties!.authenticationType.enum || []).sort((a, b) =>
        a.value!.localeCompare(b.value!)
      ),
    passwordTypes:
      properties!.passwordType &&
      (properties!.passwordType.enum || []).sort((a, b) =>
        a.value!.localeCompare(b.value!)
      ),
  };

  const onNext = (values?: any) => {
    if (values.authenticationType === 'unselected') {
      throw new Error('Invalid authentication type allowed');
    }

    history.push(
      resolvers.create.save({
        configured: { ...values, portName, serviceName },
        connectorTemplateId,
        specification,
      })
    );
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
                {({ errors, handleChange, values }) => {
                  const i18nStrings = {
                    accessTokenUrl: t(
                      'apiClientConnectors:create:security:accessTokenUrl'
                    ),
                    authenticationType: t(
                      'apiClientConnectors:create:security:authTypeLabel'
                    ),
                    authorizationUrl: t(
                      'apiClientConnectors:create:security:authorizationUrl'
                    ),
                    description: t(
                      'apiClientConnectors:create:security:description'
                    ),
                    noSecurity: t(
                      'apiClientConnectors:create:security:noSecurity'
                    ),
                    password: t('apiClientConnectors:create:security:password'),
                    passwordType: t(
                      'apiClientConnectors:create:security:passwordType'
                    ),
                    timestamp: t(
                      'apiClientConnectors:create:security:timestamp'
                    ),
                    title: t('apiClientConnectors:create:security:title'),
                    username: t('apiClientConnectors:create:security:username'),
                    usernameTokenCreated: t(
                      'apiClientConnectors:create:security:usernameTokenCreated'
                    ),
                    usernameTokenNonce: t(
                      'apiClientConnectors:create:security:usernameTokenNonce'
                    ),
                  };

                  return (
                    <ApiConnectorCreatorLayout
                      content={
                        <ApiConnectorCreatorSecurity
                          dropdowns={dropdowns}
                          errors={errors}
                          handleChange={handleChange}
                          i18n={i18nStrings}
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
                          isNextDisabled={!errors}
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
