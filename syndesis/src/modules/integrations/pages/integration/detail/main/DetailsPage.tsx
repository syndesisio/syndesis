import { Integration } from '@syndesis/models';
// import { IntegrationDetailTab } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
// import { PageTitle } from '../../../../../../containers/PageTitle';

export interface IViewIntegrationRouteState {
  /**
   * the integration object being viewed
   */
  integration: Integration;
}

export class DetailsPage extends React.Component {
  public render() {
    return (
      <WithRouteData<null, IViewIntegrationRouteState>>
        {(_, { integration }) => (
          // <IntegrationDetailTab content={}/>
          <></>
        )}
      </WithRouteData>
    );
  }
}
