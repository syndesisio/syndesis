import { getSteps } from '@syndesis/api';
import { Container, IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { PageTitle } from '../../../../../../shared';
import {
  IntegrationCreatorBreadcrumbs,
  IntegrationEditorStepAdder,
} from '../../../../components';
import {
  IBaseRouteParams,
  IBaseRouteState,
} from '../../../../components/editor/interfaces';
import resolvers from '../../../../resolvers';
import {
  getCreateAddStepHref,
  getCreateConfigureStepHrefCallback,
} from '../../../resolversHelpers';

/**
 * This page shows the integration steps configured in steps 1 and 2. It's
 * supposed to be used as the index page for step 3.
 *
 * This component expects a [state]{@link IBaseRouteState} to be properly set in
 * the route object.
 *
 * **Warning:** this component will throw an exception if the route state is
 * undefined.
 */
export class AddStepPage extends React.Component {
  public render() {
    return (
      <WithRouteData<IBaseRouteParams, IBaseRouteState>>
        {({ flow }, { integration }) => (
          <IntegrationEditorLayout
            header={<IntegrationCreatorBreadcrumbs step={3} />}
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
                    addStepHref={getCreateAddStepHref.bind(null, integration)}
                    configureStepHref={getCreateConfigureStepHrefCallback(
                      integration
                    )}
                  />
                </Container>
              </>
            }
            cancelHref={resolvers.list()}
            nextHref={resolvers.create.configure.saveAndPublish({
              integration,
            })}
          />
        )}
      </WithRouteData>
    );
  }
}
