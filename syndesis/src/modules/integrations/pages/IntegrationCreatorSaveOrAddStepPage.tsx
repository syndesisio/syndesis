import { Breadcrumb, PageHeader } from '@syndesis/ui';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { WithClosedNavigation } from '../../../containers';
import routes from '../routes';

export class IntegrationCreatorSaveOrAddStepPage extends React.Component {
  public render() {
    return (
      <WithClosedNavigation>
        <>
          <PageHeader>
            <Breadcrumb>
              <Link to={routes.integrations.list}>Integrations</Link>
              <span>New integration</span>
            </Breadcrumb>
          </PageHeader>
          <div>
            <h1>Add to Integration</h1>
            <p>
              Now you can add additional connections as well as steps to your
              integration.
            </p>
            <p>
              You can interact with the left hand panel to continue adding steps
              and connections to your integration as well.
            </p>
          </div>
        </>
      </WithClosedNavigation>
    );
  }
}
