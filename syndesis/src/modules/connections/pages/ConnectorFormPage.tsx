import { WithRouter } from '@syndesis/utils';
import { Spinner } from 'patternfly-react';
import * as React from 'react';
import { WithConnectorCreationForm } from '../containers';

export default class ConnectorFormPage extends React.Component {
  public render() {
    return (
      <WithRouter>
        {({ match }) => (
          <WithConnectorCreationForm
            connectorId={(match.params as any).connectorId}
          >
            {({ CreationForm, loading, error }) => (
              <div className={'container-fluid'}>
                {loading || error ? (
                  loading ? (
                    <Spinner loading={true} size={'lg'} />
                  ) : (
                    <p>
                      Connector not found. Perhaps we could build a form from
                      the json?
                    </p>
                  )
                ) : (
                  <CreationForm />
                )}
              </div>
            )}
          </WithConnectorCreationForm>
        )}
      </WithRouter>
    );
  }
}
