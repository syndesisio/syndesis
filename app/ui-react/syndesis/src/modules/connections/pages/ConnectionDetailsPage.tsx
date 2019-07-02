import {
  IValidationResult,
  useConnection,
  useConnectionHelpers,
  useConnectorVerifier,
} from '@syndesis/api';
import { IConnectionOverview } from '@syndesis/models';
import {
  Breadcrumb,
  ConnectionDetailsForm,
  ConnectionDetailsHeader,
  ConnectionDetailsOauthCard,
  PageLoader,
} from '@syndesis/ui';
import { useRouteData, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { UIContext } from '../../../app';
import i18n from '../../../i18n';
import { ApiError, EntityIcon, PageTitle } from '../../../shared';
import resolvers from '../../resolvers';
import { WithConnectorForm } from '../components';
import { useOAuthFlow } from '../useOAuthFlow';
import { parseValidationResult } from '../utils';

export interface IConnectionDetailsOauthProps {
  connectorId: string;
  connectionName: string;
  configuredProperties: Pick<IConnectionOverview, 'configuredProperties'>;
  onOAuthReconnect: () => void;
}
const ConnectionDetailsOAuth: React.FunctionComponent<
  IConnectionDetailsOauthProps
> = ({
  connectorId,
  connectionName,
  configuredProperties,
  onOAuthReconnect,
}) => {
  const { t } = useTranslation(['connections', 'shared']);
  const { pushNotification } = React.useContext(UIContext);
  const { connectOAuth, isConnecting } = useOAuthFlow(
    connectorId,
    connectionName,
    onOAuthReconnect
  );
  const { loading: isVerifying, read: verify } = useConnectorVerifier();

  const onOauthValidate = async () => {
    try {
      const status = await verify(connectorId, configuredProperties);
      parseValidationResult(status!, connectionName).map(({ message, type }) =>
        pushNotification(message, type)
      );
    } catch (e) {
      pushNotification(`Connection couln't be verified: ${e.message}`, 'error');
    }
  };

  return (
    <ConnectionDetailsOauthCard
      i18nTitle={t('connections:oauth:title', {
        name: connectionName,
      })}
      i18nDescription={t('connections:oauth:description')}
      i18nValidateButton={t('connections:oauth:validateButton')}
      i18nReconnectButton={t('connections:oauth:reconnectButton')}
      onValidate={onOauthValidate}
      isValidating={isVerifying}
      onReconnect={connectOAuth}
      isReconnecting={isConnecting}
    />
  );
};

export interface IConnectionDetailsRouteParams {
  connectionId: string;
}

export interface IConnectionDetailsRouteState {
  connection?: IConnectionOverview;
}

export interface IConnectionDetailsPageProps {
  edit: boolean;
}

export const ConnectionDetailsPage: React.FunctionComponent<
  IConnectionDetailsPageProps
> = ({ edit }) => {
  const { t } = useTranslation(['connections', 'shared']);
  const { pushNotification } = React.useContext(UIContext);
  const { params, state, history, location } = useRouteData<
    IConnectionDetailsRouteParams,
    IConnectionDetailsRouteState
  >();
  const [isWorking, setIsWorking] = React.useState(false);
  const {
    updateConnection,
    saveConnection,
    validateName,
  } = useConnectionHelpers();
  const { resource: connection, error, hasData } = useConnection(
    params.connectionId,
    state.connection
  );

  const getUsedByMessage = (c: IConnectionOverview): string => {
    // TODO: Schema is currently wrong as it has 'uses' as an OptionalInt. Remove cast when schema is fixed.
    const numUsedBy = c.uses as number;

    if (numUsedBy === 1) {
      return i18n.t('connections:usedByOne');
    }

    return i18n.t('connections:usedByMulti', { count: numUsedBy });
  };

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
      connection,
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
      pushNotification(t('errorSavingConnection'), 'error');
      return false;
    }
  };

  const saveDescription = async (description: string): Promise<boolean> => {
    setIsWorking(true);
    const saved = await save({ description });
    setIsWorking(false);
    return saved;
  };

  const saveName = async (name: string): Promise<boolean> => {
    let saved = false;
    setIsWorking(true);
    const validation = await doValidateName(name);
    if (validation === true) {
      saved = await save({ name });
    } else {
      pushNotification(validation, 'error');
    }
    setIsWorking(false);
    return saved;
  };

  const saveConnector = async (
    configuredProperties: {
      [key: string]: string;
    },
    actions: any
  ): Promise<void> => {
    setIsWorking(true);
    await save({ configuredProperties });
    actions.setSubmitting(false);
    setIsWorking(false);
  };

  const onOAuthReconnect = async () => {
    await save(connection);
    pushNotification(`Connection successful`, 'success');
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
      return t('shared:requiredFieldMessage') as string;
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
      return msg ? msg : 'connections:duplicateNameError';
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
        connection,
      })
    );
  };

  const startEditing = () => {
    history.push(
      resolvers.connections.connection.edit({
        connection,
      })
    );
  };

  return (
    <>
      <PageTitle title={t('connectionDetailPageTitle')} />
      <Breadcrumb>
        <Link
          data-testid={'connection-details-page-home-link'}
          to={resolvers.dashboard.root()}
        >
          {t('shared:Home')}
        </Link>
        <Link
          data-testid={'connection-details-page-connections-link'}
          to={resolvers.connections.connections()}
        >
          {t('shared:Connections')}
        </Link>
        <span>{t('connectionDetailPageTitle')}</span>
      </Breadcrumb>
      <WithLoader
        error={error !== false}
        loading={!hasData}
        loaderChildren={<PageLoader />}
        errorChildren={<ApiError error={error as Error} />}
      >
        {() => (
          <WithConnectorForm
            connector={connection.connector!}
            initialValue={connection.configuredProperties}
            disabled={!edit}
            onSave={saveConnector}
            key={location.key}
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
                <ConnectionDetailsHeader
                  allowEditing={true}
                  connectionDescription={connection.description}
                  connectionIcon={
                    <EntityIcon
                      className={'connection-details-header__connectionIcon'}
                      entity={connection}
                      alt={connection.name}
                      width={46}
                    />
                  }
                  connectionName={connection.name}
                  i18nDescriptionLabel={t('shared:Description')}
                  i18nDescriptionPlaceholder={t('descriptionPlaceholder')}
                  i18nNamePlaceholder={t('namePlaceholder')}
                  i18nUsageLabel={t('shared:Usage')}
                  i18nUsageMessage={getUsedByMessage(connection)}
                  isWorking={isWorking}
                  onChangeDescription={saveDescription}
                  onChangeName={saveName}
                />
                {!connection.derived && (
                  <ConnectionDetailsForm
                    i18nCancelLabel={t('shared:Cancel')}
                    i18nEditLabel={t('shared:Edit')}
                    i18nSaveLabel={t('shared:Save')}
                    i18nTitle={t('detailsSectionTitle', {
                      connectionName: connection.name,
                    })}
                    i18nValidateLabel={t('shared:Validate')}
                    handleSubmit={handleSubmit}
                    isValid={!dirty || isValid}
                    isWorking={isSubmitting || isValidating}
                    validationResults={validationResults}
                    isEditing={edit}
                    onCancelEditing={cancelEditing}
                    onStartEditing={startEditing}
                    onValidate={validateForm}
                  >
                    {fields}
                  </ConnectionDetailsForm>
                )}
                {connection.derived && (
                  <ConnectionDetailsOAuth
                    connectorId={connection.connector!.id!}
                    connectionName={connection.name}
                    configuredProperties={connection.configuredProperties || {}}
                    onOAuthReconnect={onOAuthReconnect}
                  />
                )}
              </>
            )}
          </WithConnectorForm>
        )}
      </WithLoader>
    </>
  );
};
