import { WithIntegrationHelpers } from '@syndesis/api';
import { Integration } from '@syndesis/models';
import { Breadcrumb, ContentWithSidebarLayout, PageHeader } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../../../../../containers';
import { PageTitle } from '../../../../../../containers/PageTitle';
import { IntegrationEditorSidebar } from '../../../../components';
import resolvers from '../../../../resolvers';
import {
  getConfigureConnectionHrefCallback,
  getConfigureStepHrefCallback,
  getCreateAddConnectionHref,
  getCreateAddStepHref,
} from '../../../resolversHelpers';

export interface ISaveOrAddStepPageState {
  forceSidebarTooltips: boolean;
}

export interface ISaveOrAddStepRouteState {
  integration: Integration;
}

export class SaveOrAddStepPage extends React.Component<
  any,
  ISaveOrAddStepPageState
> {
  public state = {
    forceSidebarTooltips: false,
  };

  constructor(props: any) {
    super(props);
    this.onAddStep = this.onAddStep.bind(this);
    this.onAddConnection = this.onAddConnection.bind(this);
    this.hideSidebarTooltips = this.hideSidebarTooltips.bind(this);
  }

  public onAddStep(e: React.MouseEvent<HTMLButtonElement>) {
    e.stopPropagation();
    this.setState({
      forceSidebarTooltips: true,
    });
  }

  public onAddConnection(e: React.MouseEvent<HTMLButtonElement>) {
    e.stopPropagation();
    this.setState({
      forceSidebarTooltips: true,
    });
  }

  public hideSidebarTooltips() {
    this.setState({
      forceSidebarTooltips: false,
    });
  }

  public render() {
    return (
      <WithClosedNavigation>
        <WithRouteData<null, ISaveOrAddStepRouteState>>
          {(_, { integration }) => (
            <ContentWithSidebarLayout
              onClick={this.hideSidebarTooltips}
              sidebar={
                <WithIntegrationHelpers>
                  {({ getSteps }) => (
                    <IntegrationEditorSidebar
                      canAdd={true}
                      steps={getSteps(integration, 0)}
                      addConnectionHref={getCreateAddConnectionHref.bind(
                        null,
                        integration
                      )}
                      addStepHref={getCreateAddStepHref.bind(null, integration)}
                      configureConnectionHref={getConfigureConnectionHrefCallback(
                        integration
                      )}
                      configureStepHref={getConfigureStepHrefCallback(
                        integration
                      )}
                      forceTooltips={this.state.forceSidebarTooltips}
                    />
                  )}
                </WithIntegrationHelpers>
              }
              content={
                <>
                  <PageTitle title={'Save or add step'} />
                  <PageHeader>
                    <Breadcrumb>
                      <Link to={resolvers.list()}>Integrations</Link>
                      <Link to={resolvers.create.start.selectConnection()}>
                        New integration
                      </Link>
                      <span>Save or add step</span>
                    </Breadcrumb>
                    <h1>Add to Integration</h1>
                    <p>
                      Now you can add additional connections as well as steps to
                      your integration.
                    </p>
                  </PageHeader>
                  <div style={{ textAlign: 'center' }}>
                    <p>
                      You can interact with the left hand panel to continue
                      adding steps and connections to your integration as well.
                    </p>
                    <div>
                      <button
                        className={'btn btn-default'}
                        onClick={this.onAddStep}
                      >
                        Add a step
                      </button>
                      <button
                        className={'btn btn-default'}
                        onClick={this.onAddConnection}
                      >
                        Add a connection
                      </button>
                    </div>
                  </div>
                </>
              }
            />
          )}
        </WithRouteData>
      </WithClosedNavigation>
    );
  }
}
