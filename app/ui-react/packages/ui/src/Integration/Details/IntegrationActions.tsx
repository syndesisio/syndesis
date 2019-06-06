import * as H from '@syndesis/history';
import { DropdownKebab } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { toValidHtmlId } from '../../helpers';
import { ButtonLink } from '../../Layout';
import './IntegrationActions.css';

export interface IIntegrationAction {
  href?: H.LocationDescriptor;
  onClick?: (e: React.MouseEvent<any>) => any;
  label: string | JSX.Element;
}

export interface IIntegrationActionsProps {
  integrationId: string;
  actions: IIntegrationAction[];
  detailsHref?: H.LocationDescriptor;
}

export class IntegrationActions extends React.Component<
  IIntegrationActionsProps
> {
  public render() {
    return (
      <>
        <ButtonLink
          data-testid={'integration-actions-view-button'}
          className="view-integration-btn"
          href={this.props.detailsHref}
          as={'default'}
        >
          View
        </ButtonLink>
        <DropdownKebab
          className="integration-actions__dropdown-kebab"
          id={`integration-${this.props.integrationId}-action-menu`}
          pullRight={true}
        >
          {this.props.actions.map((a, idx) => (
            <li role={'presentation'} key={idx}>
              {a.href ? (
                <Link
                  data-testid={`integration-actions-${toValidHtmlId(
                    a.label.toString()
                  )}`}
                  to={a.href}
                  onClick={a.onClick}
                  role={'menuitem'}
                  tabIndex={idx + 1}
                >
                  {a.label}
                </Link>
              ) : (
                <a
                  data-testid={`integration-actions-${toValidHtmlId(
                    a.label.toString()
                  )}`}
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
