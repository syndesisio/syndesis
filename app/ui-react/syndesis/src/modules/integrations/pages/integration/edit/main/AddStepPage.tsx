import { getSteps } from '@syndesis/api';
import { Integration } from '@syndesis/models';
import { Container, IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../../shared';
import {
  IntegrationEditorBreadcrumbs,
  IntegrationEditorStepAdder,
} from '../../../../components';
import resolvers from '../../../../resolvers';
import {
  getEditAddStepHref,
  getEditConfigureStepHrefCallback,
} from '../../../resolversHelpers';

export interface IAddStepRouteParams {
  flow: string;
}

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
      <WithRouteData<IAddStepRouteParams, IAddStepRouteState>>
        {({ flow }, { integration }) => (
          <IntegrationEditorLayout
            header={<IntegrationEditorBreadcrumbs step={1} />}
            content={
              <>
                <PageTitle title={'Save or add step'} />
                <Container>
                  <h1>Add to Integration</h1>
                  <p>
                    You can continue adding steps and connections to your
                    integration as well.
                  </p>
                  <IntegrationEditorStepAdder
                    steps={getSteps(integration, 0)}
                    addStepHref={getEditAddStepHref.bind(
                      null,
                      integration,
                      flow
                    )}
                    configureStepHref={getEditConfigureStepHrefCallback(
                      integration,
                      flow
                    )}
                  />
                </Container>
              </>
            }
            cancelHref={resolvers.list()}
            nextHref={resolvers.integration.edit.saveAndPublish({
              flow,
              integration,
            })}
          />
        )}
      </WithRouteData>
    );
  }
}
