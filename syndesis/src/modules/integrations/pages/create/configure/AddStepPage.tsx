import { WithIntegrationHelpers } from '@syndesis/api';
import { Integration } from '@syndesis/models';
import { IntegrationEditorLayout } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { WithClosedNavigation } from '../../../../../containers';
import { PageTitle } from '../../../../../containers/PageTitle';
import {
  IntegrationCreatorBreadcrumbs,
  IntegrationEditorStepAdder,
} from '../../../components';
import resolvers from '../../../resolvers';
import {
  getConfigureConnectionHrefCallback,
  getConfigureStepHrefCallback,
  getCreateAddConnectionHref,
  getCreateAddStepHref,
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
              header={<IntegrationCreatorBreadcrumbs step={3} />}
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
                          addConnectionHref={getCreateAddConnectionHref.bind(
                            null,
                            integration
                          )}
                          addStepHref={getCreateAddStepHref.bind(
                            null,
                            integration
                          )}
                          configureConnectionHref={getConfigureConnectionHrefCallback(
                            integration
                          )}
                          configureStepHref={getConfigureStepHrefCallback(
                            integration
                          )}
                        />
                      )}
                    </WithIntegrationHelpers>
                  </div>
                </>
              }
              cancelHref={resolvers.list()}
              backHref={resolvers.create.finish.configureAction({
                actionId: integration.flows![0].steps![
                  integration.flows![0].steps!.length - 1
                ].action!.id!,
                finishConnection: integration.flows![0].steps![
                  integration.flows![0].steps!.length - 1
                ].connection!,
                integration,
                startAction: integration.flows![0].steps![0].action!,
                startConnection: integration.flows![0].steps![0].connection!,
              })}
              nextHref={resolvers.create.configure.saveAndPublish({
                integration,
              })}
            />
          )}
        </WithRouteData>
      </WithClosedNavigation>
    );
  }
}
