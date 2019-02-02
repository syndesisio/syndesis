import { WithIntegrationHelpers } from '@syndesis/api';
import { Integration } from '@syndesis/models';
import { IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../../containers/PageTitle';
import {
  IntegrationEditorBreadcrumbs,
  IntegrationEditorStepAdder,
} from '../../../../components';
import resolvers from '../../../../resolvers';
import {
  getEditAddConnectionHref,
  getEditAddStepHref,
  getEditConfigureConnectionHrefCallback,
  getEditConfigureStepHrefCallback,
} from '../../../resolversHelpers';

export interface IAddStepRouteState {
  /**
   * the integration object to edit
   */
  integration: Integration;
}

/**
 * This page shows the steps of an existing integration.
 *
 * This component expects a [state]{@link IAddStepRouteState} to be properly set in
 * the route object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 *
 * @todo make this page shareable by making the [integration]{@link IAddStepRouteState#integration}
 * optinal and adding a WithIntegration component to retrieve the integration
 * from the backend
 */
export class AddStepPage extends React.Component {
  public render() {
    return (
      <WithRouteData<null, IAddStepRouteState>>
        {(_, { integration }) => (
          <IntegrationEditorLayout
            header={<IntegrationEditorBreadcrumbs step={1} />}
            content={
              <>
                <PageTitle title={'Save or add step'} />
                <div className={'container-fluid'}>
                  <h1>Add to Integration</h1>
                  <p>
                    You can continue adding steps and connections to your
                    integration as well.
                  </p>
                  <WithIntegrationHelpers>
                    {({ getSteps }) => (
                      <IntegrationEditorStepAdder
                        steps={getSteps(integration, 0)}
                        addConnectionHref={getEditAddConnectionHref.bind(
                          null,
                          integration
                        )}
                        addStepHref={getEditAddStepHref.bind(null, integration)}
                        configureConnectionHref={getEditConfigureConnectionHrefCallback(
                          integration
                        )}
                        configureStepHref={getEditConfigureStepHrefCallback(
                          integration
                        )}
                      />
                    )}
                  </WithIntegrationHelpers>
                </div>
              </>
            }
            cancelHref={resolvers.list()}
            nextHref={resolvers.integration.edit.saveAndPublish({
              integration,
            })}
          />
        )}
      </WithRouteData>
    );
  }
}
