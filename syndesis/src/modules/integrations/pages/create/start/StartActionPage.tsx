import { WithConnection } from '@syndesis/api';
import { ConnectionOverview } from '@syndesis/models';
import {
  IntegrationEditorLayout,
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
  Loader,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { WithClosedNavigation } from '../../../../../containers';
import { PageTitle } from '../../../../../containers/PageTitle';
import {
  IntegrationCreatorBreadcrumbs,
  IntegrationEditorChooseAction,
} from '../../../components';
import { getStartConfigureActionHref } from '../../resolversHelpers';

export interface IStartActionRouteParams {
  connectionId: string;
}

export interface IStartActionRouteState {
  connection?: ConnectionOverview;
}

export class StartActionPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithRouteData<IStartActionRouteParams, IStartActionRouteState>>
          {({ connectionId }, { connection } = {}) => {
            return (
              <WithConnection id={connectionId} initialValue={connection}>
                {({ data, hasData, error }) => (
                  <WithLoader
                    error={error}
                    loading={!hasData}
                    loaderChildren={<Loader />}
                    errorChildren={<div>TODO</div>}
                  >
                    {() => (
                      <>
                        <PageTitle title={'Choose an action'} />
                        <IntegrationEditorLayout
                          header={
                            <IntegrationCreatorBreadcrumbs
                              step={1}
                              subStep={1}
                            />
                          }
                          sidebar={
                            <IntegrationVerticalFlow disabled={true}>
                              {({ expanded }) => (
                                <>
                                  <IntegrationFlowStepGeneric
                                    icon={
                                      hasData ? (
                                        <img
                                          src={data.icon}
                                          width={24}
                                          height={24}
                                        />
                                      ) : (
                                        <Loader />
                                      )
                                    }
                                    i18nTitle={
                                      hasData
                                        ? `1. ${data.connector!.name}`
                                        : '1. Start'
                                    }
                                    i18nTooltip={
                                      hasData ? `1. ${data.name}` : 'Start'
                                    }
                                    active={true}
                                    showDetails={expanded}
                                    description={'Choose an action'}
                                  />
                                  <IntegrationFlowStepWithOverview
                                    icon={<i className={'fa fa-plus'} />}
                                    i18nTitle={'2. Finish'}
                                    i18nTooltip={'Finish'}
                                    active={false}
                                    showDetails={expanded}
                                    name={'n/a'}
                                    action={'n/a'}
                                    dataType={'n/a'}
                                  />
                                </>
                              )}
                            </IntegrationVerticalFlow>
                          }
                          content={
                            <IntegrationEditorChooseAction
                              actions={data.actionsWithFrom.sort((a, b) =>
                                a.name.localeCompare(b.name)
                              )}
                              getActionHref={getStartConfigureActionHref.bind(
                                null,
                                data
                              )}
                            />
                          }
                        />
                      </>
                    )}
                  </WithLoader>
                )}
              </WithConnection>
            );
          }}
        </WithRouteData>
      </WithClosedNavigation>
    );
  }
}
