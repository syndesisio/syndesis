import { Action, ConnectionOverview } from '@syndesis/models';
import * as H from 'history';
import { ListView } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';

export interface IIntegrationEditorChooseActionProps {
  connection: ConnectionOverview;
  actions: Action[];
  getActionHref(action: Action): H.LocationDescriptor;
}

export class IntegrationEditorChooseAction extends React.Component<
  IIntegrationEditorChooseActionProps
> {
  public render() {
    return (
      <>
        <div className="container-fluid">
          <h1>{this.props.connection.name} - Choose Action</h1>
          <p>Choose an action for the selected connection.</p>
        </div>
        <div className={'container-fluid'}>
          <ListView>
            {this.props.actions.map((a, idx) => (
              <ListView.Item
                key={idx}
                heading={a.name}
                description={a.description}
                actions={
                  <Link
                    to={this.props.getActionHref(a)}
                    className={'btn btn-default'}
                  >
                    Select
                  </Link>
                }
              />
            ))}
          </ListView>
        </div>
      </>
    );
  }
}
