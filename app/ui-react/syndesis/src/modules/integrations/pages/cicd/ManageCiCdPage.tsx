import { WithEnvironmentHelpers, WithEnvironments } from '@syndesis/api';
import { IEnvironment } from '@syndesis/api/src';
import {
  Breadcrumb,
  CiCdList,
  CiCdListEmptyState,
  CiCdListItem,
  CiCdListSkeleton,
  CiCdManagePageUI,
  IActiveFilter,
  IFilterType,
  ISortType,
  TagNameValidationError,
} from '@syndesis/ui';
import { WithListViewToolbarHelpers, WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { Link } from 'react-router-dom';
import i18n from '../../../../i18n';
import { ApiError, PageTitle } from '../../../../shared';
import resolvers from '../../resolvers';

function getFilteredAndSortedEnvironments(
  environments: IEnvironment[],
  activeFilters: IActiveFilter[],
  currentSortType: ISortType,
  isSortAscending: boolean
) {
  let answer = environments;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    answer = answer.filter(({ name }) =>
      name.toLowerCase().includes(valueToLower)
    );
  });
  answer = answer.sort(({ name: a }, { name: b }) => {
    const left = isSortAscending ? a : b;
    const right = isSortAscending ? b : a;
    return left.localeCompare(right);
  });
  return answer.map(({ name, uses }) => ({
    i18nUsesText: i18n.t('integrations:UsedByNIntegrations', { uses }),
    name,
  }));
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

function createConfirmRemoveString(name: string) {
  return i18n.t('integrations:ConfirmRemoveTag', { tag: name });
}

export interface IManageCiCdPageState {
  nameValidationError: TagNameValidationError;
}

export class ManageCiCdPage extends React.Component<{}, IManageCiCdPageState> {
  constructor(props: any) {
    super(props);
    this.state = {
      nameValidationError: TagNameValidationError.NoErrors,
    };
    this.clearNameValidation = this.clearNameValidation.bind(this);
  }
  public render() {
    return (
      <Translation ns={['integrations', 'shared']}>
        {t => (
          <WithEnvironments withUses={true}>
            {({ data, hasData, error, errorMessage, read }) => (
              <WithListViewToolbarHelpers
                defaultFilterType={filterByName}
                defaultSortType={sortByName}
              >
                {helpers => {
                  const filteredAndSortedEnvironments = getFilteredAndSortedEnvironments(
                    data as IEnvironment[],
                    helpers.activeFilters,
                    helpers.currentSortType,
                    helpers.isSortAscending
                  );
                  const handleValidateItem = (name: string) => {
                    if (!name || name === '') {
                      this.setState({
                        nameValidationError: TagNameValidationError.NoName,
                      });
                    } else if (
                      typeof data.find((item: string | IEnvironment) => {
                        if (typeof item === 'string') {
                          return (item as string) === name;
                        } else {
                          return (item as IEnvironment).name === name;
                        }
                      }) !== 'undefined'
                    ) {
                      this.setState({
                        nameValidationError: TagNameValidationError.NameInUse,
                      });
                    } else {
                      this.setState({
                        nameValidationError: TagNameValidationError.NoErrors,
                      });
                    }
                  };
                  return (
                    <WithEnvironmentHelpers>
                      {({
                        createEnvironment,
                        deleteEnvironment,
                        renameEnvironment,
                      }) => (
                        <>
                          <PageTitle title={t('integrations:ManageCiCd')} />
                          <Breadcrumb>
                            <Link
                              data-testid={'manage-cicd-page-integrations-link'}
                              to={resolvers.list()}
                            >
                              {t('shared:Integrations')}
                            </Link>
                            <span>{t('integrations:ManageCiCd')}</span>
                          </Breadcrumb>
                          <CiCdManagePageUI
                            onEditItem={(name: string, newName: string) =>
                              renameEnvironment(name, newName).finally(read)
                            }
                            onCancelEditItem={this.clearNameValidation}
                            onAddItem={(name: string) =>
                              createEnvironment(name).finally(read)
                            }
                            onCancelAddItem={this.clearNameValidation}
                            onRemoveItem={(name: string) =>
                              deleteEnvironment(name).finally(read)
                            }
                            onValidateItem={handleValidateItem}
                            filterTypes={filterTypes}
                            sortTypes={sortTypes}
                            {...helpers}
                            resultsCount={filteredAndSortedEnvironments.length}
                            i18nResultsCount={t('shared:resultsCount', {
                              count: filteredAndSortedEnvironments.length,
                            })}
                            i18nAddNewButtonText={t('shared:AddNew')}
                            i18nPageTitle={t('integrations:ManageCiCd')}
                            i18nCancelButtonText={t('shared:Cancel')}
                            i18nSaveButtonText={t('shared:Save')}
                            i18nConfirmRemoveButtonText={t('shared:Yes')}
                            i18nConfirmCancelButtonText={t('shared:No')}
                            i18nRemoveConfirmationMessage={
                              createConfirmRemoveString
                            }
                            i18nRemoveConfirmationTitle={t(
                              'shared:ConfirmRemove'
                            )}
                            i18nRemoveConfirmationDetailMessage={t(
                              'integrations:ConfirmRemoveTagDetail'
                            )}
                            i18nAddTagDialogTitle={t(
                              'integrations:AddTagDialogTitle'
                            )}
                            i18nAddTagDialogDescription={t(
                              'integrations:AddTagDialogDescription'
                            )}
                            i18nEditTagDialogTitle={t(
                              'integrations:EditTagDialogTitle'
                            )}
                            i18nEditTagDialogDescription={t(
                              'integrations:EditTagDialogDescription'
                            )}
                            i18nTagInputLabel={t('integrations:TagName')}
                            i18nPageDescription={t(
                              'integrations:ManageCiCdDescription'
                            )}
                            nameValidationError={this.state.nameValidationError}
                            i18nNoNameError={t(
                              'integrations:PleaseEnterATagName'
                            )}
                            i18nNameInUseError={t(
                              'integrations:ThatTagNameIsInUse'
                            )}
                          >
                            {({
                              openAddDialog,
                              openEditDialog,
                              openRemoveDialog,
                            }) => {
                              return (
                                <WithLoader
                                  error={error}
                                  loading={!hasData}
                                  loaderChildren={
                                    <CiCdList children={<CiCdListSkeleton />} />
                                  }
                                  errorChildren={
                                    <ApiError error={errorMessage!} />
                                  }
                                >
                                  {() => (
                                    <>
                                      {filteredAndSortedEnvironments.length !==
                                        0 && (
                                        <CiCdList
                                          children={filteredAndSortedEnvironments.map(
                                            (listItem, index) => (
                                              <CiCdListItem
                                                key={index}
                                                onEditClicked={
                                                  openEditDialog
                                                }
                                                onRemoveClicked={
                                                  openRemoveDialog
                                                }
                                                i18nEditButtonText={t(
                                                  'shared:Edit'
                                                )}
                                                i18nRemoveButtonText={t(
                                                  'shared:Remove'
                                                )}
                                                name={listItem.name}
                                                i18nUsesText={
                                                  listItem.i18nUsesText
                                                }
                                              />
                                            )
                                          )}
                                        />
                                      )}
                                      {filteredAndSortedEnvironments.length ===
                                        0 && (
                                        <CiCdListEmptyState
                                          onAddNew={openAddDialog}
                                          i18nTitle={t(
                                            'integrations:NoEnvironmentsAvailable'
                                          )}
                                          i18nAddNewButtonText={t(
                                            'shared:AddNew'
                                          )}
                                          i18nInfo={t(
                                            'integrations:NoEnvironmentsAvailableInfo'
                                          )}
                                        />
                                      )}
                                    </>
                                  )}
                                </WithLoader>
                              );
                            }}
                          </CiCdManagePageUI>
                        </>
                      )}
                    </WithEnvironmentHelpers>
                  );
                }}
              </WithListViewToolbarHelpers>
            )}
          </WithEnvironments>
        )}
      </Translation>
    );
  }
  public clearNameValidation(onClear?: () => void) {
    this.setState(
      {
        nameValidationError: TagNameValidationError.NoErrors,
      },
      onClear
    );
  }
}
