import { WithApiConnector, WithApiConnectorHelpers } from '@syndesis/api';
import { Connector } from '@syndesis/models';
import {
  ApiConnectorDetailBody,
  ApiConnectorDetailHeader,
  ApiConnectorDetailOperations,
  Breadcrumb,
  PageLoader,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { UIContext } from '../../../app';
import i18n from '../../../i18n';
import { ApiError, PageTitle } from '../../../shared';
import resolvers from '../../resolvers';

export interface IApiConnectorDetailsRouteParams {
  apiConnectorId: string;
}

export interface IApiConnectorDetailsRouteState {
  apiConnector?: Connector;
}

export interface IApiConnectorDetailsPageProps {
  edit: boolean;
}

export const ApiConnectorDetailsPage: React.FunctionComponent<IApiConnectorDetailsPageProps> = ({
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
                    <>
                      <PageTitle title={t('apiConnectorDetailsPageTitle')} />
                      <Breadcrumb>
                        <Link
                          data-testid={'api-connector-details-page-home-link'}
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
                        <span>{t('apiConnectorDetailsPageTitle')}</span>
                      </Breadcrumb>
                      <WithApiConnectorHelpers>
                        {({ saveApiConnector, updateApiConnector }) => {
                          /*
                          const handleSave = async (updated: Connector) => {
                            await saveApiConnector(updated);
                          };
                          */

                          return (
                            <WithApiConnector
                              apiConnectorId={apiConnectorId}
                              initialValue={apiConnector}
                              key={location.key}
                            >
                              {({ data, hasData, error, errorMessage }) => {
                                // tslint:disable:jsdoc-format
                                // tslint:disable

                                const onSubmit = (e: any) => {
                                  console.log(
                                    'submitted..: ' + JSON.stringify(e)
                                  );
                                };

                                /**
                                const onSubmit = async (
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
                                 **/

                                /*
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
                                 */

                                const getUsedByMessage = (
                                  c: Connector
                                ): string => {
                                  const numUsedBy = c.uses as number;
                                  if (numUsedBy === 1) {
                                    return i18n.t('usedByOne');
                                  }
                                  return i18n.t('usedByMulti', {
                                    count: numUsedBy,
                                  });
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
                                          <ApiConnectorDetailHeader
                                            i18nDescription={t(
                                              'shared:Description'
                                            )}
                                            i18nUsageLabel={t('shared:Usage')}
                                            i18nUsageMessage={getUsedByMessage(
                                              data
                                            )}
                                            connectorIcon={data.icon}
                                            connectorDescription={
                                              data.description
                                            }
                                            connectorName={data.name}
                                          />
                                          <>
                                            <ApiConnectorDetailBody
                                              basePath={
                                                (
                                                  data.configuredProperties ||
                                                  {}
                                                ).basePath
                                              }
                                              description={data.description}
                                              handleSubmit={onSubmit}
                                              host={
                                                (
                                                  data.configuredProperties ||
                                                  {}
                                                ).host
                                              }
                                              i18nCancelLabel={t(
                                                'shared:Cancel'
                                              )}
                                              i18nEditLabel={t('shared:Edit')}
                                              i18nLabelBaseUrl={t(
                                                'shared:BaseUrl'
                                              )}
                                              i18nLabelDescription={t(
                                                'shared:Description'
                                              )}
                                              i18nLabelHost={t('shared:Host')}
                                              i18nLabelName={t('shared:Name')}
                                              i18nSaveLabel={t('shared:Save')}
                                              i18nTitle={t(
                                                'detailsSectionTitle',
                                                {
                                                  connectionName: data.name,
                                                }
                                              )}
                                              icon={data.icon}
                                              name={data.name}
                                            />
                                            &nbsp;
                                            {data.actionsSummary ? (
                                              <ApiConnectorDetailOperations
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
                                                i18nNameLabel={t('shared:Name')}
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
                                              <ApiConnectorDetailOperations
                                                i18nApiDefinitionHeading={t(
                                                  'apiConnectorsPageTitle'
                                                )}
                                                i18nDescriptionLabel={t(
                                                  'shared:Description'
                                                )}
                                                i18nImportedHeading={t(
                                                  'apiConnectorsPageTitle'
                                                )}
                                                i18nNameLabel={t('shared:Name')}
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
                                          </>
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
