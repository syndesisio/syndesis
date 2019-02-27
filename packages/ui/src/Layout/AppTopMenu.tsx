// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import { Icon, Masthead } from 'patternfly-react';
import * as React from 'react';

export interface IAppTopMenuProps {
  username: string;
}

/**
 * A component to show breadcrumbs. All its children will be wrapped in a tag
 * that will automatically handle the active/inactive state by setting the
 * appropriate class to the wrapper.
 *
 * It's suggested to use only anchors or spans as children node.
 */
export const AppTopMenu: React.FunctionComponent<IAppTopMenuProps> = ({
  username,
  children,
}) => (
  <Masthead.Dropdown
    id="app-user-dropdown"
    title={[
      <span className="dropdown-title" key="dropdown-title">
        <Icon type={'fa'} name={'user'} />
        &nbsp;{username}
      </span>,
    ]}
  >
    {children}
  </Masthead.Dropdown>
);
