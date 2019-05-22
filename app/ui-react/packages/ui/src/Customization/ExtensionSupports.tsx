import {
  TextList,
  TextListItem,
  TextListItemVariants,
  TextListVariants,
} from '@patternfly/react-core';
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
          <TextList component={TextListVariants.dl}>
            {this.props.extensionActions.map(
              (action: IAction, index: number) => (
                <React.Fragment key={index}>
                  <TextListItem component={TextListItemVariants.dt}>
                    {action.name}
                  </TextListItem>
                  <TextListItem component={TextListItemVariants.dd}>
                    {action.description}
                  </TextListItem>
                </React.Fragment>
              )
            )}
          </TextList>
        ) : null}
      </div>
    );
  }
}
