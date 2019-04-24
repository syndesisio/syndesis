import { getSteps, WithConnections } from '@syndesis/api';
import { ConnectionOverview, Step } from '@syndesis/models';
import {
  ButtonLink,
  IntegrationEditorChooseConnection,
  IntegrationEditorConnectionsListItem,
  IntegrationEditorLayout,
  IntegrationsListSkeleton,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as H from 'history';
import * as React from 'react';
import { ApiError, PageTitle } from '../../../../shared';
import {
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState,
} from './interfaces';

export interface ISelectConnectionPageProps {
  cancelHref: (
    p: ISelectConnectionRouteParams,
    s: ISelectConnectionRouteState
  ) => H.LocationDescriptor;
  header: React.ReactNode;
  selectHref: (
    connection: ConnectionOverview,
    p: ISelectConnectionRouteParams,
    s: ISelectConnectionRouteState
  ) => H.LocationDescriptor;
  sidebar: (props: { steps: Step[]; activeIndex: number }) => React.ReactNode;
}

/**
 * This page shows the list of connections containing actions with a **to**
 * pattern.
 *
 * This component expects some [params]{@link ISelectConnectionRouteParams} and
 * [state]{@link ISelectConnectionRouteState} to be properly set in the route
 * object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 */
export class SelectConnectionPage extends React.Component<
  ISelectConnectionPageProps
> {
  public render() {
    return (
      <WithRouteData<ISelectConnectionRouteParams, ISelectConnectionRouteState>>
        {({ flow, position }, { integration }) => {
          const flowAsNumber = parseInt(flow, 10);
          const positionAsNumber = parseInt(position, 10);
          return (
            <>
              <PageTitle title={'Choose a connection'} />
              <IntegrationEditorLayout
                header={this.props.header}
                sidebar={this.props.sidebar({
                  activeIndex: positionAsNumber,
                  steps: getSteps(integration, flowAsNumber),
                })}
                content={
                  <WithConnections>
                    {({ data, hasData, error }) => (
                      <IntegrationEditorChooseConnection
                        i18nTitle={'Choose a connection'}
                        i18nSubtitle={
                          'Click the connection that completes the integration. If the connection you need is not available, click Create Connection.'
                        }
                      >
                        <WithLoader
                          error={error}
                          loading={!hasData}
                          loaderChildren={<IntegrationsListSkeleton />}
                          errorChildren={<ApiError />}
                        >
                          {() => (
                            <>
                              {data.connectionsWithToAction.map((c, idx) => (
                                <IntegrationEditorConnectionsListItem
                                  key={idx}
                                  integrationName={c.name}
                                  integrationDescription={
                                    c.description || 'No description available.'
                                  }
                                  icon={
                                    <img src={c.icon} width={24} height={24} />
                                  }
                                  actions={
                                    <ButtonLink
                                      href={this.props.selectHref(
                                        c,
                                        { flow, position },
                                        { integration }
                                      )}
                                    >
                                      Select
                                    </ButtonLink>
                                  }
                                />
                              ))}
                              <IntegrationEditorConnectionsListItem
                                integrationName={''}
                                integrationDescription={''}
                                icon={''}
                                actions={
                                  <ButtonLink href={'#'}>
                                    Create connection
                                  </ButtonLink>
                                }
                              />
                            </>
                          )}
                        </WithLoader>
                      </IntegrationEditorChooseConnection>
                    )}
                  </WithConnections>
                }
                cancelHref={this.props.cancelHref(
                  { flow, position },
                  { integration }
                )}
              />
            </>
          );
        }}
      </WithRouteData>
    );
  }
}
