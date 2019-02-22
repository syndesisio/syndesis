import { Action } from '@syndesis/models';
import {
  IntegrationEditorActionsList,
  IntegrationEditorActionsListItem,
} from '@syndesis/ui';
import * as H from 'history';
import * as React from 'react';
import { Link } from 'react-router-dom';

export interface IIntegrationEditorChooseActionProps {
  /**
   * The connection name to show in the content title.
   */
  connectionName: string;
  /**
   *
   */
  actions: Action[];

  /**
   *
   * @param action
   */
  getActionHref(action: Action): H.LocationDescriptor;
}

/**
 * A component to render a list of actions, to be used in the integration
 * editor.
 * @see [connectionName]{@link IIntegrationEditorChooseActionProps#connectionName}
 * @see [actions]{@link IIntegrationEditorChooseActionProps#actions}
 * @see [getActionHref]{@link IIntegrationEditorChooseActionProps#getActionHref}
 *
 * @todo perhaps change actions from being a list of `Action` to be a list of
 * `IntegrationEditorChooseActionItem` (tentative name) to decouple this
 * component from the models and move it to @syndesis/ui.
 */
export class IntegrationEditorChooseAction extends React.Component<
  IIntegrationEditorChooseActionProps
> {
  public render() {
    return (
      <>
        <div className="container-fluid">
          <h1>{this.props.connectionName} - Choose Action</h1>
          <p>Choose an action for the selected connectionName.</p>
        </div>
        <div className={'container-fluid'}>
          <IntegrationEditorActionsList>
            {this.props.actions.map((a, idx) => (
              <IntegrationEditorActionsListItem
                key={idx}
                integrationName={a.name}
                integrationDescription={
                  a.description || 'No description available.'
                }
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
          </IntegrationEditorActionsList>
        </div>
      </>
    );
  }
}
