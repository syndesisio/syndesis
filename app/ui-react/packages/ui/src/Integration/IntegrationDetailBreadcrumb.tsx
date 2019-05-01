import * as H from 'history';
import { DropdownKebab } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { Breadcrumb, ButtonLink } from '../Layout';

export interface IIntegrationMenuActions {
  href?: H.LocationDescriptor;
  onClick?: (e: React.MouseEvent<any>) => any;
  label: string | JSX.Element;
}

export interface IIntegrationDetailBreadcrumbProps {
  editHref?: H.LocationDescriptor;
  editLabel?: string | JSX.Element;
  exportAction?: (e: React.MouseEvent<any>) => any;
  exportHref?: H.LocationDescriptor;
  exportLabel?: string | JSX.Element;
  homeHref?: H.LocationDescriptor;
  i18nHome?: string;
  i18nIntegrations?: string;
  i18nPageTitle?: string;
  integrationId?: string;
  integrationsHref?: H.LocationDescriptor;
  menuActions?: IIntegrationMenuActions[];
}

export class IntegrationDetailBreadcrumb extends React.Component<
  IIntegrationDetailBreadcrumbProps
> {
  public render() {
    return (
      <Breadcrumb>
        <Link to={this.props.homeHref!}>{this.props.i18nHome}</Link>
        <Link to={this.props.integrationsHref!}>
          {this.props.i18nIntegrations}
        </Link>
        <span>{this.props.i18nPageTitle}</span>
        <div className={'pull-right'}>
          <ButtonLink
            to={this.props.exportHref}
            onClick={this.props.exportAction}
            children={this.props.exportLabel}
          />
          <ButtonLink
            href={this.props.editHref}
            children={this.props.editLabel}
          />
          <DropdownKebab
            id={`integration-${this.props.integrationId}-action-menu`}
            pullRight={true}
          >
            {this.props.menuActions
              ? this.props.menuActions.map((a, idx) => (
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
                ))
              : null}
          </DropdownKebab>
        </div>
      </Breadcrumb>
    );
  }
}
