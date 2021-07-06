import * as H from '@syndesis/history';
import * as React from 'react';

import {
  ApiConnectorCreatorBreadcrumb,
  ApiConnectorCreatorBreadSteps,
  ApiConnectorCreatorFooter,
  ApiConnectorCreatorLayout,
  ApiConnectorCreatorService,
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

export interface IServicePortRouteState {
  apiSummary: IApiSummarySoap;
  connectorTemplateId?: string;
  configured?: ICreateConnectorProps;
  specification: string;
}

export const ServicePortPage: React.FunctionComponent = () => {
  const { history } = useRouteData();
  const { state } = useRouteData<null, IServicePortRouteState>();
  const { apiSummary, connectorTemplateId, configured, specification } = state;

  /**
   * Called on 'Next' from the SOAP service & port selection form
   */
  const onServiceConfigured = (service: string, port: string) => {
    const availablePorts = JSON.parse(apiSummary!.configuredProperties!.ports);
    const availableServices = JSON.parse(
      apiSummary!.configuredProperties!.services
    );
    const firstSvc = port || availableServices[0];
    const firstPort = port || availablePorts[firstSvc][0];

    history.push(
      resolvers.create.review({
        configured: {
          portName: port || firstPort,
          serviceName: service || firstSvc,
          wsdlURL: apiSummary!.configuredProperties!.wsdlURL,
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
              <PageTitle title={'TODO'} />
              <ApiConnectorCreatorBreadcrumb
                cancelHref={resolvers.list()}
                connectorsHref={resolvers.list()}
                i18nCancel={t('shared:Cancel')}
                i18nConnectors={t('apiClientConnectors:apiConnectorsPageTitle')}
                i18nCreateConnection={t(
                  'apiClientConnectors:CreateApiConnector'
                )}
              />
              <ApiConnectorCreatorLayout
                content={
                  <ApiConnectorCreatorService
                    handleNext={onServiceConfigured}
                    i18nBtnNext={t('shared:Next')}
                    i18nPort={t('apiClientConnectors:create:soap:port')}
                    i18nService={t('apiClientConnectors:create:soap:service')}
                    i18nServicePortTitle={t(
                      'apiClientConnectors:create:soap:servicePortTitle'
                    )}
                    portName={apiSummary!.configuredProperties!.portName}
                    portsAvailable={JSON.parse(
                      apiSummary!.configuredProperties!.ports
                    )}
                    serviceName={apiSummary!.configuredProperties!.serviceName}
                    servicesAvailable={JSON.parse(
                      apiSummary!.configuredProperties!.services
                    )}
                  />
                }
                footer={
                  <ApiConnectorCreatorFooter
                    backHref={resolvers.create.review({
                      configured,
                      connectorTemplateId,
                      specification,
                    })}
                    i18nBack={t('shared:Back')}
                    i18nNext={t('shared:Next')}
                    i18nReviewEdit={t(
                      'apiClientConnectors:create:review:btnReviewEdit'
                    )}
                    isNextLoading={false}
                    isNextDisabled={!!apiSummary!.errors}
                    nextHref={resolvers.create.security({
                      configured: state.configured,
                      connectorTemplateId: state.connectorTemplateId,
                      specification: apiSummary!,
                    })}
                    reviewEditHref={
                      !state.connectorTemplateId &&
                      apiSummary?.configuredProperties
                        ? resolvers.create.specification({
                            specification:
                              apiSummary!.configuredProperties!.specification,
                          })
                        : ''
                    }
                  />
                }
                navigation={
                  <ApiConnectorCreatorBreadSteps
                    step={3}
                    i18nConfiguration={t(
                      'apiClientConnectors:create:configuration:title'
                    )}
                    i18nDetails={t('apiClientConnectors:create:details:title')}
                    i18nReview={t('apiClientConnectors:create:review:title')}
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
                    i18nDetails={t('apiClientConnectors:create:details:title')}
                    i18nReview={t('apiClientConnectors:create:review:title')}
                    i18nSecurity={t(
                      'apiClientConnectors:create:security:title'
                    )}
                    i18nSelectMethod={t(
                      'apiClientConnectors:create:selectMethod:title'
                    )}
                  />
                }
              />
            </>
          )}
        </WithLeaveConfirmation>
      )}
    </Translation>
  );
};
