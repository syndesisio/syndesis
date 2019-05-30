import {
  getConnectionIcon,
  IValidationResult,
  WithConnection,
  WithConnectionHelpers,
} from '@syndesis/api';
import { Connection } from '@syndesis/models';
import {
  Breadcrumb,
  ConnectionDetailsForm,
  ConnectionDetailsHeader,
  PageLoader,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { UIContext } from '../../../app';
import i18n from '../../../i18n';
import { ApiError } from '../../../shared';
import resolvers from '../../resolvers';
import { WithConnectorForm } from '../components';

export interface IConnectionDetailsRouteParams {
  connectionId: string;
}

export interface IConnectionDetailsRouteState {
  connection?: Connection;
}

export interface IConnectionDetailsPageProps {
  edit: boolean;
}

export interface IConnectionDetailsPageState {
  isWorking: boolean;
}

export class ConnectionDetailsPage extends React.Component<
  IConnectionDetailsPageProps,
  IConnectionDetailsPageState
> {
  public state = {
    isWorking: false,
  };

  public getUsedByMessage(connection: Connection): string {
    // TODO: Schema is currently wrong as it has 'uses' as an OptionalInt. Remove cast when schema is fixed.
    const numUsedBy = connection.uses as number;

    if (numUsedBy === 1) {
      return i18n.t('connections:usedByOne');
    }

    return i18n.t('connections:usedByMulti', { count: numUsedBy });
  }

  public render() {
    return (
      <>
        <UIContext.Consumer>
          {({ pushNotification }) => {
            return (
              <WithRouteData<
                IConnectionDetailsRouteParams,
                IConnectionDetailsRouteState
              >>
                {({ connectionId }, { connection }, { history, location }) => (
                  <Translation ns={['connections', 'shared']}>
                    {t => (
                      <WithConnectionHelpers>
                        {({
                          updateConnection,
                          saveConnection,
                          validateName,
                        }) => {
                          return (
                            <WithConnection
                              id={connectionId}
                              initialValue={connection}
                              key={location.key}
                            >
                              {({ data, hasData, error }) => {
                                const save = async ({
                                  name,
                                  description,
                                  configuredProperties,
                                }: {
                                  name?: string;
                                  description?: string;
                                  configuredProperties?: {
                                    [key: string]: string;
                                  };
                                }): Promise<boolean> => {
                                  const updatedConnection = updateConnection(
                                    data,
                                    name,
                                    description,
                                    configuredProperties
                                  );
                                  try {
                                    await saveConnection(updatedConnection);
                                    history.push(
                                      resolvers.connections.connection.details({
                                        connection: updatedConnection,
                                      })
                                    );
                                    return true;
                                  } catch (error) {
                                    pushNotification(
                                      t('errorSavingConnection'),
                                      'error'
                                    );
                                    return false;
                                  }
                                };

                                const saveDescription = async (
                                  description: string
                                ): Promise<boolean> => {
                                  this.setState({ isWorking: true });
                                  const saved = await save({ description });
                                  this.setState({ isWorking: false });
                                  return saved;
                                };

                                const saveName = async (
                                  name: string
                                ): Promise<boolean> => {
                                  let saved = false;
                                  this.setState({ isWorking: true });
                                  const validation = await doValidateName(name);
                                  if (validation === true) {
                                    saved = await save({ name });
                                  } else {
                                    pushNotification(validation, 'error');
                                  }
                                  this.setState({ isWorking: false });
                                  return saved;
                                };

                                const saveConnector = async (
                                  configuredProperties: {
                                    [key: string]: string;
                                  },
                                  actions: any
                                ): Promise<void> => {
                                  this.setState({ isWorking: true });
                                  await save({ configuredProperties });
                                  actions.setSubmitting(false);
                                  this.setState({ isWorking: false });
                                };

                                /**
                                 * Backend validation only occurs when save has been called.
                                 * @param proposedName the name to validate
                                 */
                                const doValidateName = async (
                                  proposedName: string
                                ): Promise<true | string> => {
                                  // make sure name has a value
                                  if (proposedName === '') {
                                    return t(
                                      'shared:requiredFieldMessage'
                                    ) as string;
                                  }

                                  const response: IValidationResult = await validateName(
                                    connection!,
                                    proposedName
                                  );

                                  if (!response.isError) {
                                    return true;
                                  }

                                  if (response.error === 'UniqueProperty') {
                                    const msg = t('duplicateNameError');
                                    return msg
                                      ? msg
                                      : 'connections:duplicateNameError';
                                  }

                                  return response.message
                                    ? response.message
                                    : t('errorValidatingName')
                                    ? t('errorValidatingName')!
                                    : 'connections:errorValidatingName'; // return missing i18n key
                                };

                                const cancelEditing = () => {
                                  history.push(
                                    resolvers.connections.connection.details({
                                      connection: data,
                                    })
                                  );
                                };

                                const startEditing = () => {
                                  history.push(
                                    resolvers.connections.connection.edit({
                                      connection: data,
                                    })
                                  );
                                };

                                return (
                                  <WithLoader
                                    error={error}
                                    loading={!hasData}
                                    loaderChildren={<PageLoader />}
                                    errorChildren={<ApiError />}
                                  >
                                    {() => {
                                      return (
                                        <WithConnectorForm
                                          connector={data.connector!}
                                          initialValue={
                                            data.configuredProperties
                                          }
                                          disabled={!this.props.edit}
                                          onSave={saveConnector}
                                        >
                                          {({
                                            fields,
                                            handleSubmit,
                                            validationResults,
                                            dirty,
                                            isSubmitting,
                                            isValid,
                                            isValidating,
                                            validateForm,
                                          }) => (
                                            <>
                                              <Breadcrumb>
                                                <Link
                                                  data-testid={
                                                    'connection-details-page-home-link'
                                                  }
                                                  to={resolvers.dashboard.root()}
                                                >
                                                  {t('shared:Home')}
                                                </Link>
                                                <Link
                                                  data-testid={
                                                    'connection-details-page-connections-link'
                                                  }
                                                  to={resolvers.connections.connections()}
                                                >
                                                  {t('shared:Connections')}
                                                </Link>
                                                <span>
                                                  {t(
                                                    'connectionDetailPageTitle'
                                                  )}
                                                </span>
                                              </Breadcrumb>
                                              <ConnectionDetailsHeader
                                                allowEditing={true}
                                                connectionDescription={
                                                  data.description
                                                }
                                                connectionIcon={getConnectionIcon(
                                                  process.env.PUBLIC_URL,
                                                  data
                                                )}
                                                connectionName={data.name}
                                                i18nDescriptionLabel={t(
                                                  'shared:Description'
                                                )}
                                                i18nDescriptionPlaceholder={t(
                                                  'descriptionPlaceholder'
                                                )}
                                                i18nNamePlaceholder={t(
                                                  'namePlaceholder'
                                                )}
                                                i18nUsageLabel={t(
                                                  'shared:Usage'
                                                )}
                                                i18nUsageMessage={this.getUsedByMessage(
                                                  data
                                                )}
                                                isWorking={this.state.isWorking}
                                                onChangeDescription={
                                                  saveDescription
                                                }
                                                onChangeName={saveName}
                                              />
                                              <ConnectionDetailsForm
                                                i18nCancelLabel={t(
                                                  'shared:Cancel'
                                                )}
                                                i18nEditLabel={t('shared:Edit')}
                                                i18nSaveLabel={t('shared:Save')}
                                                i18nTitle={t(
                                                  'detailsSectionTitle',
                                                  {
                                                    connectionName: data.name,
                                                  }
                                                )}
                                                i18nValidateLabel={t(
                                                  'shared:Validate'
                                                )}
                                                handleSubmit={handleSubmit}
                                                isValid={!dirty || isValid}
                                                isWorking={
                                                  isSubmitting || isValidating
                                                }
                                                validationResults={
                                                  validationResults
                                                }
                                                isEditing={this.props.edit}
                                                onCancelEditing={cancelEditing}
                                                onStartEditing={startEditing}
                                                onValidate={validateForm}
                                              >
                                                {fields}
                                              </ConnectionDetailsForm>
                                            </>
                                          )}
                                        </WithConnectorForm>
                                      );
                                    }}
                                  </WithLoader>
                                );
                              }}
                            </WithConnection>
                          );
                        }}
                      </WithConnectionHelpers>
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
