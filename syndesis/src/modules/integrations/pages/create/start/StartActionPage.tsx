import { WithConnection } from '@syndesis/api';
import { ConnectionOverview } from '@syndesis/models';
import {
  IntegrationEditorActionsListItem,
  IntegrationEditorChooseAction,
  IntegrationEditorLayout,
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
  Loader,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { PageTitle } from '../../../../../containers/PageTitle';
import { IntegrationCreatorBreadcrumbs } from '../../../components';
import resolvers from '../../../resolvers';

/**
 * @param connectionId - the ID of the connection selected in step 1.1, whose
 * actions should be shown.
 */
export interface IStartActionRouteParams {
  connectionId: string;
}

/**
 * @param connection - the connection object, whose actions should be shown. If
 * passed must equal to the [connectionId]{@link IStartActionRouteParams#connectionId}.
 * This is used to immediately show the list of actions to the user, without
 * any loader; the backend will be called nonetheless to ensure that we are
 * working with the latest data available.
 *
 * **Warning:** consistency with the connectionID is not verified!
 *
 * TODO: maybe we shouldn't hit the backend if we already have the object?
 */
export interface IStartActionRouteState {
  connection?: ConnectionOverview;
}

/**
 * This page shows the list of actions with a **from** pattern of a connection.
 * It's supposed to be used for step 1.2 of the creation wizard.
 *
 * This component expects some [url params]{@link IStartActionRouteParams}
 * and [state]{@link IStartActionRouteState} to be properly set in
 * the route object.
 *
 * @todo DRY the connection icon code
 */
export class StartActionPage extends React.Component {
  public render() {
    return (
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
                          <IntegrationCreatorBreadcrumbs step={1} subStep={1} />
                        }
                        sidebar={
                          <IntegrationVerticalFlow>
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
                            i18nTitle={`${data.name} - Choose Action`}
                            i18nSubtitle={
                              'Choose an action for the selected connectionName.'
                            }
                          >
                            {data.actionsWithFrom
                              .sort((a, b) => a.name.localeCompare(b.name))
                              .map((a, idx) => (
                                <IntegrationEditorActionsListItem
                                  key={idx}
                                  integrationName={a.name}
                                  integrationDescription={
                                    a.description || 'No description available.'
                                  }
                                  actions={
                                    <Link
                                      to={resolvers.create.start.configureAction(
                                        {
                                          actionId: a.id!,
                                          connection: data,
                                        }
                                      )}
                                      className={'btn btn-default'}
                                    >
                                      Select
                                    </Link>
                                  }
                                />
                              ))}
                          </IntegrationEditorChooseAction>
                        }
                        cancelHref={resolvers.list()}
                        backHref={resolvers.create.start.selectConnection()}
                      />
                    </>
                  )}
                </WithLoader>
              )}
            </WithConnection>
          );
        }}
      </WithRouteData>
    );
  }
}
