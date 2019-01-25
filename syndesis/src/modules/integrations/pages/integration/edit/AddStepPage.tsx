import { WithIntegrationHelpers } from '@syndesis/api';
import { Integration } from '@syndesis/models';
import { IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { WithClosedNavigation } from '../../../../../containers';
import { PageTitle } from '../../../../../containers/PageTitle';
import {
  IntegrationEditorBreadcrumbs,
  IntegrationEditorStepAdder,
} from '../../../components';
import resolvers from '../../../resolvers';
import {
  getEditConfigureConnectionHrefCallback,
  getEditConfigureStepHrefCallback,
  getEditAddConnectionHref,
  getEditAddStepHref,
} from '../../resolversHelpers';

export interface IAddStepRouteState {
  integration: Integration;
}

export class AddStepPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
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
                          addStepHref={getEditAddStepHref.bind(
                            null,
                            integration
                          )}
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
      </WithClosedNavigation>
    );
  }
}
