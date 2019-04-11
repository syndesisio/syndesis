import { Integration } from '@syndesis/models';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Translation } from 'react-i18next';
import resolvers from '../../resolvers';
import { IntegrationDetailNavBar } from '../../shared/IntegrationDetailNavBar';

/**
 * @integrationId - the ID of the integration for which details are being displayed.
 */
export interface IActivityPageParams {
  integration: Integration;
  integrationId: string;
}

export interface IActivityPageState {
  integration: Integration;
}

/**
 * This page shows the first, and default, tab of the Integration Detail page.
 *
 * This component expects an integrationId in the URL
 *
 */
export class ActivityPage extends React.Component {
  public render() {
    return (
      <WithRouteData<IActivityPageParams, IActivityPageState>>
        {({ integrationId }, { integration }, { history }) => {
          return (
            <div>
              <Translation ns={['integration', 'shared']}>
                {t => (
                  <IntegrationDetailNavBar
                    detailsTabHref={resolvers.integration.details({
                      integration,
                    })}
                    activityTabHref={resolvers.integration.details({
                      integration,
                    })}
                  />
                )}
              </Translation>
              <p>This is the Integration Detail Activity page.</p>
            </div>
          );
        }}
      </WithRouteData>
    );
  }
}
