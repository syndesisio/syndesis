import { WithConnection, WithIntegrationHelpers } from '@syndesis/api';
import { ConnectionOverview, Integration } from '@syndesis/models';
import { Breadcrumb, ContentWithSidebarLayout, Loader } from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../../../../../containers';
import {
  IntegrationEditorChooseAction,
  IntegrationEditorSidebar,
} from '../../../../components';
import resolvers from '../../../../resolvers';
import {
  getConfigureConnectionHrefCallback,
  getConfigureStepHrefCallback,
  getEditConfigureActionHref,
} from '../../../resolversHelpers';

export interface ISelectActionRouteParams {
  connectionId: string;
  position: string;
}

export interface ISelectActionRouteState {
  connection: ConnectionOverview;
  integration: Integration;
}

export class SelectActionPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <WithRouteData<ISelectActionRouteParams, ISelectActionRouteState>>
          {({ connectionId, position }, { connection, integration }) => {
            const positionAsNumber = parseInt(position, 10);
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
                      <ContentWithSidebarLayout
                        sidebar={
                          <WithIntegrationHelpers>
                            {({ getSteps }) => (
                              <IntegrationEditorSidebar
                                steps={getSteps(integration, 0)}
                                configureConnectionHref={getConfigureConnectionHrefCallback(
                                  integration
                                )}
                                configureStepHref={getConfigureStepHrefCallback(
                                  integration
                                )}
                                activeIndex={positionAsNumber}
                              />
                            )}
                          </WithIntegrationHelpers>
                        }
                        content={
                          <IntegrationEditorChooseAction
                            breadcrumb={
                              <Breadcrumb>
                                <Link to={resolvers.list()}>Integrations</Link>
                                <Link
                                  to={resolvers.create.start.selectConnection()}
                                >
                                  New integration
                                </Link>
                                <Link
                                  to={resolvers.create.configure.index({
                                    integration,
                                  })}
                                >
                                  Save or add step
                                </Link>
                                <span>Choose action</span>
                              </Breadcrumb>
                            }
                            actions={(positionAsNumber > 0
                              ? data.actionsWithTo
                              : data.actionsWithFrom
                            ).sort((a, b) => a.name.localeCompare(b.name))}
                            getActionHref={getEditConfigureActionHref.bind(
                              null,
                              position,
                              integration
                            )}
                          />
                        }
                      />
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
