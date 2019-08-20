import { ApiContext, WithOAuthAppHelpers, WithOAuthApps } from '@syndesis/api';
import { AutoForm, IFormValue } from '@syndesis/auto-form';
import { OAuthApp } from '@syndesis/models';
import {
  ConfirmationButtonStyle,
  ConfirmationDialog,
  ConfirmationIconType,
  IActiveFilter,
  IFilterType,
  IntegrationsListSkeleton,
  ISortType,
  OAuthAppExpanderBody,
  OAuthAppHeader,
  OAuthAppList,
  OAuthAppListItem,
  OAuthAppListItemView,
} from '@syndesis/ui';
import {
  allFieldsRequired,
  getRequiredStatusText,
  toFormDefinition,
  validateRequiredProperties,
  WithListViewToolbarHelpers,
  WithLoader,
} from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import i18n from '../../../i18n';
import { ApiError, EntityIcon, PageTitle } from '../../../shared';

function getFilteredAndSortedOAuthApps(
  oauthApps: OAuthApp[],
  activeFilters: IActiveFilter[],
  currentSortType: ISortType,
  isSortAscending: boolean
) {
  let answer = oauthApps;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    answer = answer.filter(oauthApp =>
      oauthApp.name.toLowerCase().includes(valueToLower)
    );
  });
  answer = answer.sort((a, b) => {
    const left = isSortAscending ? a : b;
    const right = isSortAscending ? b : a;
    return left.name.localeCompare(right.name);
  });
  return answer;
}

const filterByName = {
  filterType: 'text',
  id: 'name',
  placeholder: i18n.t('shared:filterByNamePlaceholder'),
  title: i18n.t('shared:Name'),
} as IFilterType;

const filterTypes = [filterByName];

const sortByName = {
  id: 'name',
  isNumeric: false,
  title: i18n.t('shared:Name'),
} as ISortType;

const sortTypes: ISortType[] = [sortByName];

export interface IOAuthAppsPageState {
  showRemoveDialog: boolean;
  currentItemName?: string;
  currentItemId?: string;
  saveSuccessId?: string;
}

export class OAuthAppsPage extends React.Component<{}, IOAuthAppsPageState> {
  constructor(props: {}) {
    super(props);
    this.state = {
      showRemoveDialog: false,
    };
    this.closeRemoveDialog = this.closeRemoveDialog.bind(this);
    this.handleRemoveConfirm = this.handleRemoveConfirm.bind(this);
  }
  public closeRemoveDialog() {
    this.setState({ showRemoveDialog: false });
  }
  public handleRemoveClicked(name: string, id: string) {
    this.setState({
      currentItemId: id,
      currentItemName: name,
      showRemoveDialog: true,
    });
  }
  public handleRemoveConfirm(
    deleteOAuthApp: (id: string) => Promise<void>,
    read: () => Promise<void>
  ) {
    const id = this.state.currentItemId;
    this.closeRemoveDialog();
    deleteOAuthApp(id!).then(read);
  }
  public handleSave(id: string, submitForm: () => void) {
    this.setState({ currentItemId: id });
    submitForm();
  }
  public render() {
    return (
      <Translation ns={['settings', 'shared']}>
        {t => (
          <ApiContext.Consumer>
            {({ apiUri }) => (
              <WithOAuthAppHelpers>
                {({ updateOAuthApp, deleteOAuthApp }) => (
                  <WithOAuthApps disableUpdates={true}>
                    {({ data, hasData, error, errorMessage, read }) => (
                      <WithListViewToolbarHelpers
                        defaultFilterType={filterByName}
                        defaultSortType={sortByName}
                      >
                        {helpers => {
                          const filteredAndSortedOAuthApps = getFilteredAndSortedOAuthApps(
                            data.items!,
                            helpers.activeFilters,
                            helpers.currentSortType,
                            helpers.isSortAscending
                          );

                          return (
                            <>
                              <PageTitle
                                title={t('settings:OAuthApplicationManagement')}
                              />
                              <OAuthAppHeader
                                i18nPageTitle={t(
                                  'settings:OAuthApplicationManagement'
                                )}
                                i18nDescription={t(
                                  'settings:OAuthApplicationManagementPageDescription',
                                  {
                                    docLink: i18n.t(
                                      'shared:links.oauth-access'
                                    ),
                                  }
                                )}
                                i18nCallbackDescription={t(
                                  'settings:OAuthCallbackDescription'
                                )}
                                callbackURI={`${apiUri}/credentials/callback`}
                              />
                              {this.state.showRemoveDialog && (
                                <ConfirmationDialog
                                  buttonStyle={ConfirmationButtonStyle.DANGER}
                                  icon={ConfirmationIconType.DANGER}
                                  i18nCancelButtonText={t('shared:Cancel')}
                                  i18nConfirmButtonText={t('shared:Remove')}
                                  i18nConfirmationMessage={t(
                                    'settings:RemoveOAuthAppCredentials',
                                    {
                                      name: this.state.currentItemName!,
                                    }
                                  )}
                                  i18nTitle={t('settings:RemoveOAuthAppTitle')}
                                  showDialog={this.state.showRemoveDialog}
                                  onCancel={this.closeRemoveDialog}
                                  onConfirm={() =>
                                    this.handleRemoveConfirm(
                                      deleteOAuthApp,
                                      read
                                    )
                                  }
                                />
                              )}
                              <OAuthAppListItemView
                                filterTypes={filterTypes}
                                sortTypes={sortTypes}
                                {...helpers}
                                resultsCount={filteredAndSortedOAuthApps.length}
                                i18nResultsCount={t('shared:resultsCount', {
                                  count: filteredAndSortedOAuthApps.length,
                                })}
                              >
                                <WithLoader
                                  error={error}
                                  loading={!hasData}
                                  loaderChildren={<IntegrationsListSkeleton />}
                                  errorChildren={
                                    <ApiError error={errorMessage!} />
                                  }
                                >
                                  {() => (
                                    <OAuthAppList>
                                      {filteredAndSortedOAuthApps.map(
                                        (oauthApp, index) => {
                                          const definition = oauthApp.properties!;
                                          const configured =
                                            typeof oauthApp.configuredProperties !==
                                            'undefined';
                                          const configuration =
                                            oauthApp.configuredProperties;
                                          const key = JSON.stringify(
                                            configuration
                                          );
                                          const requiredPrompt = getRequiredStatusText(
                                            definition,
                                            i18n.t('shared:AllFieldsRequired'),
                                            i18n.t(
                                              'shared:FieldsMarkedWithStarRequired'
                                            ),
                                            ''
                                          );
                                          const validator = (
                                            values: IFormValue
                                          ) =>
                                            validateRequiredProperties(
                                              definition,
                                              (name: string) =>
                                                `${name} is required`,
                                              values
                                            );
                                          return (
                                            <OAuthAppListItem
                                              key={index}
                                              configured={configured}
                                              expanded={
                                                this.state.currentItemId ===
                                                oauthApp.id
                                              }
                                              icon={
                                                <EntityIcon
                                                  entity={oauthApp}
                                                  alt={oauthApp.name}
                                                  width={30}
                                                  height={30}
                                                />
                                              }
                                              i18nNotConfiguredText={t(
                                                'settings:OAuthAppNotConfigured'
                                              )}
                                              id={oauthApp.id!}
                                              name={oauthApp.name!}
                                            >
                                              <AutoForm<IFormValue>
                                                key={index + '-' + key}
                                                definition={toFormDefinition(
                                                  definition
                                                )}
                                                allFieldsRequired={allFieldsRequired(
                                                  definition
                                                )}
                                                i18nFieldsStatusText={
                                                  requiredPrompt
                                                }
                                                i18nRequiredProperty={t(
                                                  'shared:requiredFieldMessage'
                                                )}
                                                validate={validator}
                                                validateInitial={validator}
                                                initialValue={
                                                  configuration as IFormValue
                                                }
                                                onSave={(
                                                  configuredProperties,
                                                  actions
                                                ) => {
                                                  updateOAuthApp({
                                                    ...oauthApp,
                                                    configuredProperties,
                                                  })
                                                    .then(() => {
                                                      this.setState({
                                                        saveSuccessId:
                                                          oauthApp.id,
                                                      });
                                                    })
                                                    .finally(() => {
                                                      actions.setSubmitting(
                                                        false
                                                      );
                                                      read();
                                                    });
                                                }}
                                              >
                                                {({
                                                  dirty,
                                                  fields,
                                                  isValid,
                                                  submitForm,
                                                }) => (
                                                  <OAuthAppExpanderBody
                                                    disableSave={
                                                      !dirty || !isValid
                                                    }
                                                    disableRemove={!configured}
                                                    showSuccess={
                                                      oauthApp.id ===
                                                      this.state.saveSuccessId
                                                    }
                                                    onSave={() => {
                                                      this.handleSave(
                                                        oauthApp.id!,
                                                        submitForm
                                                      );
                                                    }}
                                                    onRemove={() => {
                                                      this.handleRemoveClicked(
                                                        oauthApp.name,
                                                        oauthApp.id!
                                                      );
                                                    }}
                                                    i18nAlertTitle={t(
                                                      'settings:RegistrationSuccessful'
                                                    )}
                                                    i18nAlertDetail={t(
                                                      'settings:RegistrationSuccessAlertDetail',
                                                      {
                                                        name: oauthApp.name,
                                                      }
                                                    )}
                                                    i18nRemoveButtonText={t(
                                                      'shared:Remove'
                                                    )}
                                                    i18nSaveButtonText={t(
                                                      'shared:Save'
                                                    )}
                                                  >
                                                    {fields}
                                                  </OAuthAppExpanderBody>
                                                )}
                                              </AutoForm>
                                            </OAuthAppListItem>
                                          );
                                        }
                                      )}
                                    </OAuthAppList>
                                  )}
                                </WithLoader>
                              </OAuthAppListItemView>
                            </>
                          );
                        }}
                      </WithListViewToolbarHelpers>
                    )}
                  </WithOAuthApps>
                )}
              </WithOAuthAppHelpers>
            )}
          </ApiContext.Consumer>
        )}
      </Translation>
    );
  }
}
