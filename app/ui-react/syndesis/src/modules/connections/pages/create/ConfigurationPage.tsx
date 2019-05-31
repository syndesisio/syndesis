import { WithConnector } from '@syndesis/api';
import * as H from '@syndesis/history';
import { Connector } from '@syndesis/models';
import {
  ConnectionCreatorLayout,
  ConnectorConfigurationForm,
  PageLoader,
  PageSection,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { ApiError, PageTitle } from '../../../../shared';
import { WithLeaveConfirmation } from '../../../../shared/WithLeaveConfirmation';
import {
  ConnectionCreatorBreadSteps,
  WithConnectorForm,
} from '../../components';
import { ConnectionCreatorBreadcrumb } from '../../components/ConnectionCreatorBreadcrumb';
import resolvers from '../../resolvers';
import routes from '../../routes';

export interface IConfigurationPageRouteParams {
  connectorId: string;
}

export interface IConfigurationPageRouteState {
  connector?: Connector;
}

export default class ConfigurationPage extends React.Component {
  public render() {
    return (
      <Translation ns={['connections', 'shared']}>
        {t => (
          <WithLeaveConfirmation
            i18nTitle={t('unsavedChangesTitle')}
            i18nConfirmationMessage={t('unsavedChangesMessage')}
            shouldDisplayDialog={(location: H.LocationDescriptor) => {
              const url =
                typeof location === 'string' ? location : location.pathname!;
              return !url.startsWith(routes.create.root);
            }}
          >
            {() => (
              <WithRouteData<
                IConfigurationPageRouteParams,
                IConfigurationPageRouteState
              >>
                {({ connectorId }, { connector }, { history }) => (
                  <WithConnector id={connectorId} initialValue={connector}>
                    {({ data, hasData, error }) => (
                      <WithLoader
                        error={error}
                        loading={!hasData}
                        loaderChildren={<PageLoader />}
                        errorChildren={<ApiError />}
                      >
                        {() => {
                          const onSave = (configuredProperties: {
                            [key: string]: string;
                          }) => {
                            history.push(
                              resolvers.create.review({
                                configuredProperties,
                                connector: data,
                              })
                            );
                          };
                          return (
                            <WithConnectorForm connector={data} onSave={onSave}>
                              {({
                                fields,
                                handleSubmit,
                                validationResults,
                                submitForm,
                                isSubmitting,
                                isValid,
                                isValidating,
                                validateForm,
                              }) => {
                                return (
                                  <>
                                    <PageTitle title={'Configure connection'} />
                                    <ConnectionCreatorBreadcrumb
                                      cancelHref={resolvers.connections()}
                                    />
                                    <ConnectionCreatorLayout
                                      header={
                                        <ConnectionCreatorBreadSteps step={2} />
                                      }
                                      content={
                                        <PageSection>
                                          <ConnectorConfigurationForm
                                            i18nFormTitle={data.name}
                                            handleSubmit={handleSubmit}
                                            backHref={resolvers.create.selectConnector()}
                                            onNext={submitForm}
                                            isNextDisabled={
                                              isSubmitting || !isValid
                                            }
                                            isNextLoading={isSubmitting}
                                            isValidating={isValidating}
                                            onValidate={(
                                              ev: React.FormEvent
                                            ) => {
                                              ev.preventDefault();
                                              validateForm();
                                            }}
                                            validationResults={
                                              validationResults
                                            }
                                            isLastStep={false}
                                          >
                                            {fields}
                                          </ConnectorConfigurationForm>
                                        </PageSection>
                                      }
                                    />
                                  </>
                                );
                              }}
                            </WithConnectorForm>
                          );
                        }}
                      </WithLoader>
                    )}
                  </WithConnector>
                )}
              </WithRouteData>
            )}
          </WithLeaveConfirmation>
        )}
      </Translation>
    );
  }
}
