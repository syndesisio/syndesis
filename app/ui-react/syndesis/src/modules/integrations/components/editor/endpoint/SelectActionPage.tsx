import { getSteps, WithConnection } from '@syndesis/api';
import * as H from '@syndesis/history';
import { IConnectionWithIconFile, Step } from '@syndesis/models';
import {
  ButtonLink,
  IntegrationEditorActionsListItem,
  IntegrationEditorChooseAction,
  IntegrationEditorLayout,
  Loader,
} from '@syndesis/ui';
import { WithLoader, WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { ApiError, PageTitle } from '../../../../../shared';
import {
  ISelectActionRouteParams,
  ISelectActionRouteState,
} from '../interfaces';

export interface ISelectActionPageProps {
  cancelHref: (
    p: ISelectActionRouteParams,
    s: ISelectActionRouteState
  ) => H.LocationDescriptor;
  sidebar: (props: {
    steps: Step[];
    activeIndex: number;
    connection: IConnectionWithIconFile;
  }) => React.ReactNode;
  selectHref: (
    actionId: string,
    p: ISelectActionRouteParams,
    s: ISelectActionRouteState
  ) => H.LocationDescriptor;
}

/**
 * This page shows the list of actions of a connection containing either a
 * **to** or **from pattern, depending on the specified [position]{@link ISelectActionRouteParams#position}.
 *
 * This component expects some [params]{@link ISelectActionRouteParams} and
 * [state]{@link ISelectActionRouteState} to be properly set in the route
 * object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 */
export class SelectActionPage extends React.Component<ISelectActionPageProps> {
  public render() {
    return (
      <WithRouteData<ISelectActionRouteParams, ISelectActionRouteState>>
        {({ connectionId, flowId, position }, { connection, integration }) => {
          const positionAsNumber = parseInt(position, 10);
          return (
            <WithConnection id={connectionId} initialValue={connection}>
              {({ data, hasData, error }) => (
                <WithLoader
                  error={error}
                  loading={!hasData}
                  loaderChildren={<Loader />}
                  errorChildren={<ApiError />}
                >
                  {() => (
                    <>
                      <PageTitle title={'Choose an action'} />
                      <IntegrationEditorLayout
                        sidebar={this.props.sidebar({
                          activeIndex: positionAsNumber,
                          connection: connection as IConnectionWithIconFile,
                          steps: getSteps(integration, flowId),
                        })}
                        content={
                          <IntegrationEditorChooseAction
                            i18nTitle={`${connection.name} - Choose Action`}
                            i18nSubtitle={
                              'Choose an action for the selected connectionName.'
                            }
                          >
                            {(positionAsNumber > 0
                              ? data.actionsWithTo
                              : data.actionsWithFrom
                            )
                              .sort((a, b) => a.name.localeCompare(b.name))
                              .map((a, idx) => (
                                <IntegrationEditorActionsListItem
                                  key={idx}
                                  integrationName={a.name}
                                  integrationDescription={
                                    a.description || 'No description available.'
                                  }
                                  actions={
                                    <ButtonLink
                                      href={this.props.selectHref(
                                        a.id!,
                                        { connectionId, flowId, position },
                                        { connection, integration }
                                      )}
                                    >
                                      Select
                                    </ButtonLink>
                                  }
                                />
                              ))}
                          </IntegrationEditorChooseAction>
                        }
                        cancelHref={this.props.cancelHref(
                          { connectionId, flowId, position },
                          { connection, integration }
                        )}
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
