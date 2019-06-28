import { WithApiConnector, WithApiConnectorHelpers } from '@syndesis/api';
import { Connector } from '@syndesis/models';
import {
  ApiConnectorDetailCard,
  ApiConnectorReview,
  Breadcrumb,
  ButtonLink,
  Container,
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
import { ApiError } from '../../../shared';
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

export default class ApiConnectorDetailsPage extends React.Component<
  IApiConnectorDetailsPageProps
> {
  public getUsedByMessage(apiConnector: Connector): string {
    // TODO: Schema is currently wrong as it has 'uses' as an OptionalInt. Remove cast when schema is fixed.
    const numUsedBy = apiConnector.uses as number;

    if (numUsedBy === 1) {
      return i18n.t('apiClientConnectors:usedByOne');
    }

    return i18n.t('apiClientConnectors:usedByMulti', { count: numUsedBy });
  }

  public render() {
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
          }
        );
      }

      return undefined;
    };

    return (
      <>
        <UIContext.Consumer>
          {({ pushNotification }) => {
            return (
              <WithRouteData<
                IApiConnectorDetailsRouteParams,
                IApiConnectorDetailsRouteState
              >>
                {(
                  { apiConnectorId },
                  { apiConnector },
                  { history, location }
                ) => (
                  <Translation ns={['apiClientConnectors', 'shared']}>
                    {t => (
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
                                const handleSubmit = async (
                                  values: IConnectorValues,
                                  actions: any
                                ) => {
                                  const updated = updateApiConnector(
                                    data,
                                    values.name,
                                    values.description,
                                    values.host,
                                    values.basePath,
                                    values.icon
                                  );

                                  try {
                                    await handleSave(updated);
                                    actions.setSubmitting(false);
                                    history.push(
                                      resolvers.apiClientConnectors.apiConnector.details(
                                        {
                                          apiConnector: updated,
                                        }
                                      )
                                    );
                                    return true;
                                  } catch (error) {
                                    actions.setSubmitting(false);
                                    pushNotification(
                                      t('errorSavingApiConnector'),
                                      'error'
                                    );
                                    return false;
                                  }
                                };

                                const cancelEditing = () => {
                                  history.push(
                                    resolvers.apiClientConnectors.apiConnector.details(
                                      {
                                        apiConnector: data,
                                      }
                                    )
                                  );
                                };

                                const startEditing = () => {
                                  history.push(
                                    resolvers.apiClientConnectors.apiConnector.edit(
                                      {
                                        apiConnector: data,
                                      }
                                    )
                                  );
                                };

                                return (
                                  <WithLoader
                                    error={error}
                                    loading={!hasData}
                                    loaderChildren={<PageLoader />}
                                    errorChildren={
                                      <ApiError error={errorMessage!} />
                                    }
                                  >
                                    {() => {
                                      return (
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
                                                'apiConnectorDetailsPageTitle'
                                              )}
                                            </span>
                                          </Breadcrumb>
                                          <PageSection>
                                            <Container className="col-sm-4">
                                              <ApiConnectorDetailCard
                                                description={data.description}
                                                icon={data.icon}
                                                name={data.name}
                                              />
                                            </Container>
                                            <Container className="col-sm-8">
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
                                                isEditing={this.props.edit}
                                                handleSubmit={handleSubmit}
                                              >
                                                {({
                                                  submitForm,
                                                  isSubmitting,
                                                  isUploadingImage,
                                                }) =>
                                                  this.props.edit ? (
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
                                                        onClick={submitForm}
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
                                                  )
                                                }
                                              </ApiConnectorInfoForm>
                                              &nbsp;
                                              {data.actionsSummary ? (
                                                <ApiConnectorReview
                                                  apiConnectorDescription={
                                                    data.description
                                                  }
                                                  apiConnectorName={data.name}
                                                  i18nApiDefinitionHeading={t(
                                                    'reviewApiDefinitionHeading'
                                                  )}
                                                  i18nDescriptionLabel={t(
                                                    'shared:Description'
                                                  )}
                                                  i18nErrorsHeading={t(
                                                    'reviewErrorsHeading',
                                                    {
                                                      count: 0,
                                                    }
                                                  )} // TODO fix count
                                                  i18nImportedHeading={t(
                                                    'reviewImportedHeading'
                                                  )}
                                                  i18nNameLabel={t(
                                                    'shared:Name'
                                                  )}
                                                  i18nOperationsHtmlMessage={t(
                                                    'reviewOperationsMessage',
                                                    {
                                                      count:
                                                        data.actionsSummary
                                                          .totalActions || 0,
                                                    }
                                                  )}
                                                  i18nOperationTagHtmlMessages={getTagMessages(
                                                    data
                                                  )}
                                                  i18nTitle={t(
                                                    'reviewActionsTitle'
                                                  )}
                                                  i18nWarningsHeading={t(
                                                    'reviewWarningsHeading',
                                                    {
                                                      count: 0,
                                                    }
                                                  )} // TODO fix count
                                                />
                                              ) : (
                                                <ApiConnectorReview
                                                  i18nApiDefinitionHeading={t(
                                                    'apiConnectorsPageTitle'
                                                  )}
                                                  i18nDescriptionLabel={t(
                                                    'shared:Description'
                                                  )}
                                                  i18nImportedHeading={t(
                                                    'apiConnectorsPageTitle'
                                                  )}
                                                  i18nNameLabel={t(
                                                    'shared:Name'
                                                  )}
                                                  i18nOperationsHtmlMessage={t(
                                                    'reviewOperationsMessage',
                                                    {
                                                      count: 0,
                                                    }
                                                  )}
                                                  i18nTitle={t(
                                                    'reviewActionsTitle'
                                                  )}
                                                  i18nValidationFallbackMessage={t(
                                                    'reviewValidationFallback'
                                                  )}
                                                />
                                              )}
                                            </Container>
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
                    )}
                  </Translation>
                )}
              </WithRouteData>
            );
          }}
        </UIContext.Consumer>
      </>
    );
  }
}
