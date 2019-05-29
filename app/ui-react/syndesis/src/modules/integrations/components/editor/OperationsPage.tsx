import * as H from '@syndesis/history';
import { Flow } from '@syndesis/models';
import {
  ApiProviderReviewOperations,
  ApiProviderReviewOperationsItem,
  IActiveFilter,
  IFilterType,
  IntegrationEditorLayout,
  ISortType,
} from '@syndesis/ui';
import { useRouteData, WithListViewToolbarHelpers } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import i18n from '../../../../i18n';
import { PageTitle } from '../../../../shared';
import {
  IBaseFlowRouteParams,
  IBaseRouteParams,
  IBaseRouteState,
  IPageWithEditorBreadcrumb,
} from './interfaces';

interface IOperationFlow extends Flow {
  implemented: number;
  method: string;
  description: string;
}

function getFilteredAndSortedIntegrations(
  flows: IOperationFlow[],
  activeFilters: IActiveFilter[],
  currentSortType: ISortType,
  isSortAscending: boolean
) {
  let filteredAndSortedFlows = flows;
  activeFilters.forEach((filter: IActiveFilter) => {
    const valueToLower = filter.value.toLowerCase();
    filteredAndSortedFlows = filteredAndSortedFlows.filter(
      (f: IOperationFlow) => {
        if (filter.id === 'name') {
          return f.name.toLowerCase().includes(valueToLower);
        }
        if (filter.id === 'method') {
          return f.method!.toLowerCase().includes(valueToLower);
        }
        return false;
      }
    );
  });

  filteredAndSortedFlows = filteredAndSortedFlows.sort((fA, fB) => {
    const left = isSortAscending ? fA : fB;
    const right = isSortAscending ? fB : fA;
    if (currentSortType.id === 'name') {
      return left.name.localeCompare(right.name);
    } else if (currentSortType.id === 'method') {
      return left.method.localeCompare(right.method);
    } else {
      return left.implemented - right.implemented;
    }
  });

  return filteredAndSortedFlows;
}

const filterByName = {
  filterType: 'text',
  id: 'name',
  placeholder: i18n.t('integrations:toolbars:filterByOperationNamePlaceholder'),
  title: i18n.t('integrations:toolbars:OperationName'),
} as IFilterType;

const filterByMethod = {
  filterType: 'text',
  id: 'method',
  placeholder: i18n.t('integrations:toolbars:filterByMethodPlaceholder'),
  title: i18n.t('integrations:toolbars:Method'),
} as IFilterType;

const sortByName = {
  id: 'name',
  isNumeric: false,
  title: i18n.t('integrations:toolbars:OperationName'),
} as ISortType;

const sortByMethod = {
  id: 'method',
  isNumeric: false,
  title: i18n.t('integrations:toolbars:Method'),
} as ISortType;

const sortByImplemented = {
  id: 'implemented',
  isNumeric: true,
  title: i18n.t('integrations:toolbars:OperationImplemented'),
} as ISortType;

const sortTypes: ISortType[] = [sortByName, sortByMethod, sortByImplemented];

export interface IOperationsPageProps extends IPageWithEditorBreadcrumb {
  cancelHref: (p: IBaseRouteParams, s: IBaseRouteState) => H.LocationDescriptor;
  saveHref: (p: IBaseRouteParams, s: IBaseRouteState) => H.LocationDescriptor;
  getFlowHref: (
    p: IBaseFlowRouteParams,
    s: IBaseRouteState
  ) => H.LocationDescriptor;
}

/**
 * This is usually the final step of the API Provider user flow.
 * This page shows the operations that have been previously defined
 * earlier in the user flow.
 */
export const OperationsPage: React.FunctionComponent<IOperationsPageProps> = ({
  getBreadcrumb,
  cancelHref,
  getFlowHref,
  saveHref,
}) => {
  const { params, state } = useRouteData<IBaseRouteParams, IBaseRouteState>();
  const flows = state
    .integration!.flows!.filter(f => f.metadata && f.metadata.excerpt)
    .map(f => {
      const [method, description] = (f.description || '').split(' ');
      return {
        ...f,
        description,
        implemented: f.metadata!['return-code-edited'] ? 1 : 0,
        method,
      } as IOperationFlow;
    });

  return (
    <Translation ns={['integrations', 'shared']}>
      {t => (
        <>
          <PageTitle
            title={t('integrations:apiProvider:reviewOperations:title')}
          />
          <IntegrationEditorLayout
            title={t('integrations:apiProvider:reviewOperations:title')}
            description={t(
              'integrations:apiProvider:reviewOperations:description'
            )}
            toolbar={getBreadcrumb(
              t('integrations:apiProvider:reviewOperations:title'),
              params,
              state
            )}
            content={
              <WithListViewToolbarHelpers
                defaultFilterType={filterByName}
                defaultSortType={sortByImplemented}
                defaultSortAscending={false}
              >
                {helpers => {
                  const filteredAndSortedFlows = getFilteredAndSortedIntegrations(
                    flows,
                    helpers.activeFilters,
                    helpers.currentSortType,
                    helpers.isSortAscending
                  );
                  return (
                    <ApiProviderReviewOperations
                      filterTypes={[filterByName, filterByMethod]}
                      sortTypes={sortTypes}
                      resultsCount={filteredAndSortedFlows.length}
                      i18nResultsCount={t('shared:resultsCount', {
                        count: filteredAndSortedFlows.length,
                      })}
                      {...helpers}
                    >
                      {filteredAndSortedFlows.map((f, idx) => (
                        <ApiProviderReviewOperationsItem
                          key={idx}
                          createFlowHref={getFlowHref(
                            {
                              ...params,
                              flowId: f.id!,
                            },
                            state
                          )}
                          createAsPrimary={!f.implemented}
                          i18nCreateFlow={
                            f.implemented ? 'Edit flow' : 'Create flow'
                          }
                          operationDescription={f.name}
                          operationHttpMethod={f.method}
                          operationPath={f.description}
                        />
                      ))}
                    </ApiProviderReviewOperations>
                  );
                }}
              </WithListViewToolbarHelpers>
            }
            cancelHref={cancelHref(params, state)}
            saveHref={saveHref(params, state)}
            publishHref={saveHref(params, state)}
          />
        </>
      )}
    </Translation>
  );
};
