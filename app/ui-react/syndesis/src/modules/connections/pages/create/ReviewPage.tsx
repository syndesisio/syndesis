import { WithConnectionHelpers } from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import * as H from '@syndesis/history';
import { Connector } from '@syndesis/models';
import {
  ConnectionCreatorLayout,
  ConnectorConfigurationForm,
  PageSection,
} from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { UIContext } from '../../../../app';
import { PageTitle } from '../../../../shared';
import { WithLeaveConfirmation } from '../../../../shared/WithLeaveConfirmation';
import { ConnectionCreatorBreadSteps } from '../../components';
import { ConnectionCreatorBreadcrumb } from '../../components/ConnectionCreatorBreadcrumb';
import resolvers from '../../resolvers';
import routes from '../../routes';

export interface ISaveForm {
  name: string;
  description?: string;
}

export interface IReviewPageRouteState {
  connector: Connector;
  configuredProperties: { [key: string]: string };
}

export default class ReviewPage extends React.Component {
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
            {({ allowNavigation }) => (
              <UIContext.Consumer>
                {({ pushNotification }) => (
                  <WithRouteData<null, IReviewPageRouteState>>
                    {(_, { connector, configuredProperties }, { history }) => (
                      <WithConnectionHelpers>
                        {({ createConnection, saveConnection }) => {
                          const onSave = async (
                            { name, description }: ISaveForm,
                            actions: any
                          ) => {
                            try {
                              const connection = createConnection(
                                connector,
                                name,
                                description || '',
                                configuredProperties
                              );
                              await saveConnection(connection);
                              actions.setSubmitting(false);
                              pushNotification(
                                `<strong>Connection created</strong> Connection <strong>${name}</strong> successfully created`,
                                'success'
                              );
                              allowNavigation();
                              history.push(resolvers.connections());
                            } catch (e) {
                              pushNotification(e.message, 'error');
                            }
                          };
                          const definition: IFormDefinition = {
                            name: {
                              defaultValue: '',
                              displayName: 'Name',
                              required: true,
                              type: 'string',
                            },
                            /* tslint:disable-next-line:object-literal-sort-keys */
                            description: {
                              defaultValue: '',
                              displayName: 'Description',
                              type: 'textarea',
                            },
                          };
                          return (
                            <AutoForm<ISaveForm>
                              i18nRequiredProperty={'* Required field'}
                              definition={definition}
                              initialValue={{
                                description: '',
                                name: '',
                              }}
                              onSave={onSave}
                            >
                              {({
                                fields,
                                handleSubmit,
                                isSubmitting,
                                isValid,
                                submitForm,
                              }) => (
                                <>
                                  <PageTitle title={'Name connection'} />
                                  <ConnectionCreatorBreadcrumb
                                    cancelHref={resolvers.connections()}
                                  />
                                  <ConnectionCreatorLayout
                                    header={
                                      <ConnectionCreatorBreadSteps step={3} />
                                    }
                                    content={
                                      <PageSection>
                                        <ConnectorConfigurationForm
                                          i18nFormTitle={'Name connection'}
                                          handleSubmit={handleSubmit}
                                          backHref={resolvers.create.configureConnector(
                                            {
                                              connector,
                                            }
                                          )}
                                          onNext={submitForm}
                                          isNextDisabled={!isValid}
                                          isNextLoading={isSubmitting}
                                          isValidating={false}
                                          isLastStep={true}
                                        >
                                          {fields}
                                        </ConnectorConfigurationForm>
                                      </PageSection>
                                    }
                                  />
                                </>
                              )}
                            </AutoForm>
                          );
                        }}
                      </WithConnectionHelpers>
                    )}
                  </WithRouteData>
                )}
              </UIContext.Consumer>
            )}
          </WithLeaveConfirmation>
        )}
      </Translation>
    );
  }
}
