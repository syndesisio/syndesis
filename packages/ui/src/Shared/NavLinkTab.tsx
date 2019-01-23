import classnames from 'classnames';
import * as React from 'react';
import { NavLink } from 'react-router-dom';
import './NavLinkTab.css';

export interface INavLinkTabProps {
  // `true` if the link should not be navigated too.
  disableLink: boolean;

  // The text of link displayed to the user.
  i18nLinkTitle: string;

  // The URL string to use when the link is clicked.
  toLink: string;
}

// A class to represent a tab. Use multiple to create a tab folder. A tab can be disabled when its
// link should not be navigated too.
export class NavLinkTab extends React.Component<INavLinkTabProps> {
  public constructor(props: INavLinkTabProps) {
    super(props);
    this.handleClick = this.handleClick.bind(this);
  }

  public handleClick(event: React.MouseEvent<HTMLAnchorElement>) {
    if (this.props && this.props.disableLink) {
      event.preventDefault();
    }
  }

  public render() {
    return (
      <NavLink
        className={classnames('NavLinkTab', {
          'is-disabled': this.props.disableLink,
        })}
        to={this.props.toLink}
        onClick={this.handleClick}
      >
        {this.props.i18nLinkTitle}
      </NavLink>
    );
  }
}
