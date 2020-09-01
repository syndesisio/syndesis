import { useApiConnectorSummary } from '@syndesis/api';
import {
  ApiConnectorCreatorBreadcrumb,
  ApiConnectorCreatorBreadSteps,
  ApiConnectorCreatorLayout,
  ApiConnectorCreatorSelectMethod,
  ApiConnectorCreatorService,
  ApiConnectorCreatorToggleList,
} from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { PageTitle } from '../../../../shared';
import resolvers from '../../resolvers';

export const SelectMethodPage: React.FunctionComponent = () => {
  const { history } = useRouteData();
  const [connectorTemplateId, setConnectorTemplateId] = React.useState('');
  const [spec, setSpec] = React.useState('');
  const [showSoapConfig, setShowSoapConfig] = React.useState(false);
  const { apiSummary } = useApiConnectorSummary(spec, connectorTemplateId);

  const onServiceConfigured = (service: string, port: string) => {
    history.push(
      resolvers.create.review({
        configured: {
          portName: port,
          serviceName: service,
          wsdlUrl: apiSummary!.configuredProperties!.wsdlUrl,
        },
        connectorTemplateId,
        specification: spec,
      })
    );
  };

  const onNext = (specification: string, connectorTemplate?: string) => {
    if (!connectorTemplate) {
      history.push(resolvers.create.review({ specification }));
    } else {
      setSpec(specification);
      setConnectorTemplateId(connectorTemplate);
      setShowSoapConfig(true);
    }
  };

  return (
    <Translation ns={['apiClientConnectors', 'shared']}>
      {t => (
        <>
          <PageTitle
            title={t('apiClientConnectors:create:selectMethod:title')}
          />
          <ApiConnectorCreatorBreadcrumb
            i18nCancel={t('shared:Cancel')}
            i18nConnectors={t('apiClientConnectors:apiConnectorsPageTitle')}
            i18nCreateConnection={t('apiClientConnectors:CreateApiConnector')}
            cancelHref={resolvers.list()}
            connectorsHref={resolvers.list()}
          />
          <ApiConnectorCreatorLayout
            content={
              <>
                {!showSoapConfig && (
                  <ApiConnectorCreatorSelectMethod
                    disableDropzone={false}
                    fileExtensions={t(
                      'apiClientConnectors:create:selectMethod:dndFileExtensions'
                    )}
                    i18nBtnNext={t('shared:Next')}
                    i18nHelpMessage={t(
                      'apiClientConnectors:create:selectMethod:dndHelpMessage'
                    )}
                    i18nInstructions={t(
                      'apiClientConnectors:create:selectMethod:dndInstructions'
                    )}
                    i18nNoFileSelectedMessage={t(
                      'apiClientConnectors:create:selectMethod:dndNoFileSelectedLabel'
                    )}
                    i18nSelectedFileLabel={t(
                      'apiClientConnectors:create:selectMethod:dndSelectedFileLabel'
                    )}
                    i18nUploadFailedMessage={t(
                      'apiClientConnectors:create:selectMethod:dndUploadFailedMessage'
                    )}
                    i18nUploadSuccessMessage={t(
                      'apiClientConnectors:create:selectMethod:dndUploadSuccessMessage'
                    )}
                    i18nMethodFromFile={t(
                      'apiClientConnectors:create:selectMethod:methodFromFile'
                    )}
                    i18nMethodFromUrl={t(
                      'apiClientConnectors:create:selectMethod:methodFromUrl'
                    )}
                    i18nUrlNote={t(
                      'apiClientConnectors:create:selectMethod:urlNote'
                    )}
                    onNext={onNext}
                  />
                )}
                {/* Where users can specify a SOAP service and port if connector is WSDL file */}
                {showSoapConfig && apiSummary && (
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
                )}
              </>
            }
            navigation={
              <ApiConnectorCreatorBreadSteps
                step={1}
                i18nDetails={t('apiClientConnectors:create:details:title')}
                i18nReview={t('apiClientConnectors:create:review:title')}
                i18nSecurity={t('apiClientConnectors:create:security:title')}
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
                i18nSecurity={t('apiClientConnectors:create:security:title')}
                i18nSelectMethod={t(
                  'apiClientConnectors:create:selectMethod:title'
                )}
              />
            }
          />
        </>
      )}
    </Translation>
  );
};
