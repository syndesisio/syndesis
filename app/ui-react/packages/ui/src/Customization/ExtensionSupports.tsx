import { Row } from 'patternfly-react';
import * as React from 'react';

export interface IAction {
  description: string;
  name: string;
}

export interface IExtensionSupportsProps {
  extensionActions: IAction[];
}

/**
 * A component that displays the support section of the extension details page.
 */
export class ExtensionSupports extends React.Component<
  IExtensionSupportsProps
> {
  public render() {
    return (
      <div className="extension-group">
        {this.props.extensionActions.length !== 0 ? (
          <Row>
            <div className="col-xs-offset-1 col-xs-11">
              {this.props.extensionActions.map(
                (action: IAction, index: number) => (
                  <div key={index}>
                    <strong>{action.name}</strong> - {action.description}
                  </div>
                )
              )}
            </div>
          </Row>
        ) : null}
      </div>
    );
  }
}
