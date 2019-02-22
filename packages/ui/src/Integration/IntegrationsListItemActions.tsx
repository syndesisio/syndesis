import * as H from 'history';
import { DropdownKebab } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';

export interface IIntegrationAction {
  href?: H.LocationDescriptor;
  onClick?: (e: React.MouseEvent<any>) => any;
  label: string | JSX.Element;
}

export interface IIntegrationsListItemActionsProps {
  integrationId: string;
  actions: IIntegrationAction[];
}

export class IntegrationsListItemActions extends React.Component<
  IIntegrationsListItemActionsProps
> {
  public render() {
    return (
      <>
        <Link to={'#todo'} className={'btn btn-default'}>
          View
        </Link>
        <DropdownKebab
          id={`integration-${this.props.integrationId}-action-menu`}
          pullRight={true}
        >
          {this.props.actions.map((a, idx) => (
            <li role={'presentation'} key={idx}>
              {a.href ? (
                <Link
                  to={a.href}
                  onClick={a.onClick}
                  role={'menuitem'}
                  tabIndex={idx + 1}
                >
                  {a.label}
                </Link>
              ) : (
                <a
                  href={'javascript:void(0)'}
                  onClick={a.onClick}
                  role={'menuitem'}
                  tabIndex={idx + 1}
                >
                  {a.label}
                </a>
              )}
            </li>
          ))}
        </DropdownKebab>
      </>
    );
  }
}
