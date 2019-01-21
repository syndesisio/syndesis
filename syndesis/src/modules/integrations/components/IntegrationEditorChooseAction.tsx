import { Action } from '@syndesis/models';
import * as H from 'history';
import { ListView } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';

export interface IIntegrationEditorChooseActionProps {
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
          <ListView>
            {this.props.actions.map((a, idx) => (
              <Link
                to={this.props.getActionHref(a)}
                style={{
                  color: 'inherit',
                  textDecoration: 'none',
                }}
                key={idx}
              >
                <ListView.Item heading={a.name} description={a.description} />
              </Link>
            ))}
          </ListView>
        </div>
      </>
    );
  }
}
