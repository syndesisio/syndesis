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
 * A function component that displays the support section of the extension details page.
 */
export const ExtensionSupports: React.FunctionComponent<
  IExtensionSupportsProps
> = props => {
  return (
    <div className="extension-group">
      {props.extensionActions.length !== 0 ? (
        <TextList component={TextListVariants.dl}>
          {props.extensionActions.map((action: IAction, index: number) => (
            <React.Fragment key={index}>
              <TextListItem component={TextListItemVariants.dt}>
                {action.name}
              </TextListItem>
              <TextListItem component={TextListItemVariants.dd}>
                {action.description}
              </TextListItem>
            </React.Fragment>
          ))}
        </TextList>
      ) : null}
    </div>
  );
};
