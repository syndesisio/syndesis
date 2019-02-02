import { WithConnections, WithIntegrationHelpers } from '@syndesis/api';
import { Integration } from '@syndesis/models';
import { IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../../containers/PageTitle';
import {
  IntegrationCreatorBreadcrumbs,
  IntegrationEditorChooseConnection,
  IntegrationEditorSidebar,
} from '../../../../components';
import resolvers from '../../../../resolvers';
import { getCreateSelectActionHref } from '../../../resolversHelpers';

/**
 * @param position - the zero-based position for the new step in the integration
 * flow.
 */
export interface ISelectConnectionRouteParams {
  position: string;
}

/**
 * @param integration - the integration object coming from step 3.index, used to
 * render the IVP.
 */
export interface ISelectConnectionRouteState {
  integration: Integration;
}

/**
 * This page shows the list of connections containing actions with a **to**
 * pattern.
 * It's supposed to be used for 3.add.1 of the creation wizard.
 *
 * This component expects some [params]{@link ISelectConnectionRouteParams} and
 * [state]{@link ISelectConnectionRouteState} to be properly set in the route
 * object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 */
export class SelectConnectionPage extends React.Component {
  public render() {
    return (
      <WithRouteData<ISelectConnectionRouteParams, ISelectConnectionRouteState>>
        {({ position }, { integration }) => (
          <>
            <PageTitle title={'Choose a connection'} />
            <IntegrationEditorLayout
              header={<IntegrationCreatorBreadcrumbs step={3} />}
              sidebar={
                <WithIntegrationHelpers>
                  {({ getSteps }) => {
                    const positionAsNumber = parseInt(position, 10);
                    return (
                      <IntegrationEditorSidebar
                        steps={getSteps(integration, 0)}
                        addAtIndex={positionAsNumber}
                        addI18nTitle={`${positionAsNumber + 1}. Start`}
                        addI18nTooltip={'Start'}
                        addI18nDescription={'Choose a connection'}
                      />
                    );
                  }}
                </WithIntegrationHelpers>
              }
              content={
                <WithConnections>
                  {({ data, hasData, error }) => (
                    <IntegrationEditorChooseConnection
                      connections={data.connectionsWithToAction}
                      loading={!hasData}
                      error={error}
                      i18nTitle={'Choose a connection'}
                      i18nSubtitle={
                        'Click the connection that completes the integration. If the connection you need is not available, click Create Connection.'
                      }
                      getConnectionHref={getCreateSelectActionHref.bind(
                        null,
                        position,
                        integration
                      )}
                    />
                  )}
                </WithConnections>
              }
              cancelHref={resolvers.create.configure.index({ integration })}
            />
          </>
        )}
      </WithRouteData>
    );
  }
}
