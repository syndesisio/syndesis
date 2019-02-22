import { Connector } from '@syndesis/models';
import { Loader } from '@syndesis/ui';
import { WithRouteData } from '@syndesis/utils';
import * as React from 'react';
import { WithConnectorCreationForm } from '../containers';

export interface IConnectorFormPageRouteParams {
  connectorId: string;
}

export interface IConnectorFormPageRouteState {
  connector?: Connector;
}

export default class ConnectorFormPage extends React.Component {
  public render() {
    return (
      <WithRouteData<
        IConnectorFormPageRouteParams,
        IConnectorFormPageRouteState
      >>
        {({ connectorId }, { connector }) => (
          <WithConnectorCreationForm connectorId={connectorId}>
            {({ CreationForm, loading, error }) => (
              <div className={'container-fluid'}>
                {loading || error ? (
                  loading ? (
                    <Loader size={'lg'} />
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
      </WithRouteData>
    );
  }
}
