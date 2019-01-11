import { Action } from '@syndesis/models';
import { PageHeader } from '@syndesis/ui';
import * as H from 'history';
import { ListView } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';

export interface IIntegrationEditorChooseActionProps {
  breadcrumb: JSX.Element;
  actions: Action[];
  getActionHref(action: Action): H.LocationDescriptor;
}

export class IntegrationEditorChooseAction extends React.Component<
  IIntegrationEditorChooseActionProps
> {
  public render() {
    return (
      <>
        <PageHeader>
          {this.props.breadcrumb}
          <h1>Choose Action</h1>
          <p>Choose an action for the selected connection.</p>
        </PageHeader>
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
