import { Action, ConnectionOverview } from '@syndesis/models';
import { IntegrationActionSelectorCard } from '@syndesis/ui';
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
          <h1>Choose Action</h1>
          <p>Choose an action for the selected connection.</p>
        </div>
        <div className={'container-fluid'}>
          <IntegrationActionSelectorCard
            content={
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
            }
            title={this.props.connection.name}
          />
        </div>
      </>
    );
  }
}
