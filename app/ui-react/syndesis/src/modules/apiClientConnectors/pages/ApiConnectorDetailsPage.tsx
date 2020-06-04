import { WithApiConnector, WithApiConnectorHelpers } from '@syndesis/api';
import { Connector } from '@syndesis/models';
import {
  ApiConnectorDetailCard,
  ApiConnectorDetailsForm,
  ApiConnectorReview,
  Breadcrumb,
  ButtonLink,
  Loader,
  PageLoader,
  PageSection,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { UIContext } from '../../../app';
import i18n from '../../../i18n';
import { ApiError, PageTitle } from '../../../shared';
import resolvers from '../../resolvers';
import { ApiConnectorInfoForm, IConnectorValues } from '../components';

export interface IApiConnectorDetailsRouteParams {
  apiConnectorId: string;
}

export interface IApiConnectorDetailsRouteState {
  apiConnector?: Connector;
}

export interface IApiConnectorDetailsPageProps {
  edit: boolean;
}

export const ApiConnectorDetailsPage: React.FunctionComponent<IApiConnectorDetailsPageProps> = (
  {
    edit,
  }) => {

  const getTagMessages = (apiConnector: Connector): string[] | undefined => {
    if (
      apiConnector.actionsSummary &&
      apiConnector.actionsSummary.actionCountByTags
    ) {
      return Object.keys(apiConnector.actionsSummary.actionCountByTags).map(
        tagName => {
          const numTagged = apiConnector.actionsSummary!.actionCountByTags![
            tagName
            ];
          return i18n.t('apiClientConnectors:reviewOperationsTaggedMessage', {
            count: numTagged,
            tag: tagName,
          });
        },
      );
    }

    return undefined;
  };

  return (
    <>
      <UIContext.Consumer>
        {({ pushNotification }) => {
          return (
            <WithRouteData<IApiConnectorDetailsRouteParams,
                IApiConnectorDetailsRouteState>>
              {(
                { apiConnectorId },
                { apiConnector },
                { history, location },
              ) => (
                <Translation ns={['apiClientConnectors', 'shared']}>
                  {t => (
                    <>
                      <Breadcrumb>
                        <Link
                          data-testid={
                            'api-connector-details-page-home-link'
                          }
                          to={resolvers.dashboard.root()}
                        >
                          {t('shared:Home')}
                        </Link>
                        <Link
                          data-testid={
                            'api-connector-details-page-api-connectors-link'
                          }
                          to={resolvers.apiClientConnectors.list()}
                        >
                          {t('apiConnectorsPageTitle')}
                        </Link>
                        <span>
                                              {t(
                                                'apiConnectorDetailsPageTitle',
                                              )}
                                            </span>
                      </Breadcrumb>
                      <PageTitle title={t('apiConnectorDetailsPageTitle')}/>
                      <WithApiConnectorHelpers>
                        {({ saveApiConnector, updateApiConnector }) => {
                          const handleSave = async (updated: Connector) => {
                            await saveApiConnector(updated);
                          };

                          return (
                            <WithApiConnector
                              apiConnectorId={apiConnectorId}
                              initialValue={apiConnector}
                              key={location.key}
                            >
                              {({ data, hasData, error, errorMessage }) => {
                                const onSubmit = async (
                                  values: IConnectorValues,
                                  actions: any,
                                ) => {
                                  const updated = updateApiConnector(
                                    data,
                                    values.name,
                                    values.description,
                                    values.host,
                                    values.basePath,
                                    values.icon,
                                  );

                                  try {
                                    await handleSave(updated);
                                    actions.setSubmitting(false);
                                    history.push(
                                      resolvers.apiClientConnectors.apiConnector.details(
                                        {
                                          apiConnector: updated,
                                        },
                                      ),
                                    );
                                    return true;
                                  } catch (error) {
                                    actions.setSubmitting(false);
                                    pushNotification(
                                      t('errorSavingApiConnector'),
                                      'error',
                                    );
                                    return false;
                                  }
                                };

                                const cancelEditing = () => {
                                  history.push(
                                    resolvers.apiClientConnectors.apiConnector.details(
                                      {
                                        apiConnector: data,
                                      },
                                    ),
                                  );
                                };

                                const startEditing = () => {
                                  history.push(
                                    resolvers.apiClientConnectors.apiConnector.edit(
                                      {
                                        apiConnector: data,
                                      },
                                    ),
                                  );
                                };

                                return (
                                  <WithLoader
                                    error={error}
                                    loading={!hasData}
                                    loaderChildren={<PageLoader/>}
                                    errorChildren={
                                      <ApiError error={errorMessage!}/>
                                    }
                                  >
                                    {() => {
                                      return (
                                        <>
                                          <PageSection style={{ height: '10em' }}>
                                            <ApiConnectorDetailCard
                                              description={data.description}
                                              icon={data.icon}
                                              name={data.name}
                                            />
                                            <>
                                              <ApiConnectorInfoForm
                                                name={data.name}
                                                description={data.description}
                                                basePath={
                                                  (
                                                    data.configuredProperties ||
                                                    {}
                                                  ).basePath
                                                }
                                                host={
                                                  (
                                                    data.configuredProperties ||
                                                    {}
                                                  ).host
                                                }
                                                apiConnectorIcon={data.icon}
                                                handleSubmit={onSubmit}
                                              >
                                                {({
                                                    connectorName,
                                                    fields,
                                                    handleSubmit,
                                                    icon,
                                                    isSubmitting,
                                                    isUploadingImage,
                                                    onUploadImage,
                                                  }) => {
                                                  return (
                                                    <>
                                                      <ApiConnectorDetailsForm
                                                        apiConnectorIcon={icon}
                                                        apiConnectorName={connectorName}
                                                        i18nIconLabel={t('ConnectorIcon')}
                                                        handleSubmit={handleSubmit}
                                                        onUploadImage={onUploadImage}
                                                        isEditing={edit}
                                                        fields={fields}
                                                      />
                                                      {edit ? (
                                                        <>
                                                          <ButtonLink
                                                            data-testid={
                                                              'api-connector-details-form-cancel-button'
                                                            }
                                                            className="api-connector-details-form__editButton"
                                                            disabled={
                                                              isSubmitting ||
                                                              isUploadingImage
                                                            }
                                                            onClick={cancelEditing}
                                                          >
                                                            {t('shared:Cancel')}
                                                          </ButtonLink>
                                                          <ButtonLink
                                                            data-testid={
                                                              'api-connector-details-form-save-button'
                                                            }
                                                            as="primary"
                                                            className="api-connector-details-form__editButton"
                                                            disabled={
                                                              isSubmitting ||
                                                              isUploadingImage
                                                            }
                                                            onClick={handleSubmit}
                                                          >
                                                            {(isSubmitting ||
                                                              isUploadingImage) && (
                                                              <Loader
                                                                size={'sm'}
                                                                inline={true}
                                                              />
                                                            )}
                                                            {t('shared:Save')}
                                                          </ButtonLink>
                                                        </>
                                                      ) : (
                                                        <ButtonLink
                                                          data-testid={
                                                            'api-connector-details-form-edit-button'
                                                          }
                                                          as="primary"
                                                          onClick={startEditing}
                                                        >
                                                          {t('shared:Edit')}
                                                        </ButtonLink>
                                                      )}
                                                    </>
                                                  );
                                                }}
                                              </ApiConnectorInfoForm>
                                              &nbsp;
                                              {data.actionsSummary ? (
                                                <ApiConnectorReview
                                                  apiConnectorDescription={
                                                    data.description
                                                  }
                                                  apiConnectorName={data.name}
                                                  i18nApiDefinitionHeading={t(
                                                    'reviewApiDefinitionHeading',
                                                  )}
                                                  i18nDescriptionLabel={t(
                                                    'shared:Description',
                                                  )}
                                                  i18nErrorsHeading={t(
                                                    'reviewErrorsHeading',
                                                    {
                                                      count: 0,
                                                    },
                                                  )} // TODO fix count
                                                  i18nImportedHeading={t(
                                                    'reviewImportedHeading',
                                                  )}
                                                  i18nNameLabel={t(
                                                    'shared:Name',
                                                  )}
                                                  i18nOperationsHtmlMessage={t(
                                                    'reviewOperationsMessage',
                                                    {
                                                      count:
                                                        data.actionsSummary
                                                          .totalActions || 0,
                                                    },
                                                  )}
                                                  i18nOperationTagHtmlMessages={getTagMessages(
                                                    data,
                                                  )}
                                                  i18nTitle={t(
                                                    'reviewActionsTitle',
                                                  )}
                                                  i18nWarningsHeading={t(
                                                    'reviewWarningsHeading',
                                                    {
                                                      count: 0,
                                                    },
                                                  )} // TODO fix count
                                                />
                                              ) : (
                                                <ApiConnectorReview
                                                  i18nApiDefinitionHeading={t(
                                                    'apiConnectorsPageTitle',
                                                  )}
                                                  i18nDescriptionLabel={t(
                                                    'shared:Description',
                                                  )}
                                                  i18nImportedHeading={t(
                                                    'apiConnectorsPageTitle',
                                                  )}
                                                  i18nNameLabel={t(
                                                    'shared:Name',
                                                  )}
                                                  i18nOperationsHtmlMessage={t(
                                                    'reviewOperationsMessage',
                                                    {
                                                      count: 0,
                                                    },
                                                  )}
                                                  i18nTitle={t(
                                                    'reviewActionsTitle',
                                                  )}
                                                  i18nValidationFallbackMessage={t(
                                                    'reviewValidationFallback',
                                                  )}
                                                />
                                              )}
                                            </>
                                          </PageSection>
                                        </>
                                      );
                                    }}
                                  </WithLoader>
                                );
                              }}
                            </WithApiConnector>
                          );
                        }}
                      </WithApiConnectorHelpers>
                    </>
                  )}
                </Translation>
              )}
            </WithRouteData>
          );
        }}
      </UIContext.Consumer>
    </>
  );
};
