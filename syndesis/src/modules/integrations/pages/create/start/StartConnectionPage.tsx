import { WithConnections } from '@syndesis/api';
import {
  IntegrationEditorChooseConnection,
  IntegrationEditorConnectionsListItem,
  IntegrationEditorLayout,
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationsListSkeleton,
  IntegrationVerticalFlow,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { PageTitle } from '../../../../../containers/PageTitle';
import { IntegrationCreatorBreadcrumbs } from '../../../components';
import resolvers from '../../../resolvers';

/**
 * This page shows the list of connections containing actions with a **from**
 * pattern.
 * It's supposed to be used for step 1.1 of the creation wizard.
 *
 * This component doesn't expect any URL parameter or state.
 */
export class StartConnectionPage extends React.Component {
  public render() {
    return (
      <IntegrationEditorLayout
        header={<IntegrationCreatorBreadcrumbs step={1} />}
        sidebar={
          <IntegrationVerticalFlow>
            {({ expanded }) => (
              <>
                <IntegrationFlowStepGeneric
                  icon={<i className={'fa fa-plus'} />}
                  i18nTitle={'1. Start'}
                  i18nTooltip={'Start'}
                  active={true}
                  showDetails={expanded}
                  description={'Choose a connection'}
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
          <WithConnections>
            {({ data, hasData, error }) => (
              <>
                <PageTitle title={'New Integration'} />
                <IntegrationEditorChooseConnection
                  i18nTitle={'Choose a Start Connection'}
                  i18nSubtitle={
                    'Click the connection that starts the integration. If the connection you need is not available, click Create Connection.'
                  }
                >
                  <WithLoader
                    error={error}
                    loading={!hasData}
                    loaderChildren={<IntegrationsListSkeleton />}
                    errorChildren={<div>TODO</div>}
                  >
                    {() => (
                      <>
                        {data.connectionsWithFromAction.map((c, idx) => (
                          <IntegrationEditorConnectionsListItem
                            key={idx}
                            integrationName={c.name}
                            integrationDescription={
                              c.description || 'No description available.'
                            }
                            icon={<img src={c.icon} width={24} height={24} />}
                            actions={
                              <Link
                                to={resolvers.create.start.selectAction({
                                  connection: c,
                                })}
                                className={'btn btn-default'}
                              >
                                Select
                              </Link>
                            }
                          />
                        ))}
                        <IntegrationEditorConnectionsListItem
                          integrationName={''}
                          integrationDescription={''}
                          icon={''}
                          actions={
                            <Link to={'#'} className={'btn btn-default'}>
                              Create connection
                            </Link>
                          }
                        />
                      </>
                    )}
                  </WithLoader>
                </IntegrationEditorChooseConnection>
              </>
            )}
          </WithConnections>
        }
        cancelHref={resolvers.list()}
      />
    );
  }
}
