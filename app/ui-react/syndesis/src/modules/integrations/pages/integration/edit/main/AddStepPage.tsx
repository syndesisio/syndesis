import { getSteps } from '@syndesis/api';
import { Container, IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../../shared';
import {
  IntegrationEditorBreadcrumbs,
  IntegrationEditorStepAdder,
} from '../../../../components';
import resolvers from '../../../../resolvers';
import { IBaseRouteParams, IBaseRouteState } from '../../../editorInterfaces';
import {
  getEditAddStepHref,
  getEditConfigureStepHrefCallback,
} from '../../../resolversHelpers';

/**
 * This page shows the steps of an existing integration.
 *
 * This component expects a [state]{@link IBaseRouteState} to be properly set in
 * the route object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 *
 * @todo make this page shareable by making the [integration]{@link IBaseRouteState#integration}
 * optinal and adding a WithIntegration component to retrieve the integration
 * from the backend
 */
export class AddStepPage extends React.Component {
  public render() {
    return (
      <WithRouteData<IBaseRouteParams, IBaseRouteState>>
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
