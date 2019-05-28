import * as H from '@syndesis/history';
import { DropdownKebab } from 'patternfly-react';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { toValidHtmlId } from '../../helpers';
import { Breadcrumb, ButtonLink } from '../../Layout';
import { IMenuActions } from '../../Shared';
import './IntegrationDetailBreadcrumb.css';

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
  menuActions?: IMenuActions[];
}

export class IntegrationDetailBreadcrumb extends React.Component<
  IIntegrationDetailBreadcrumbProps
> {
  public render() {
    return (
      <Breadcrumb
        actions={
          <>
            <ButtonLink
              data-testid={'integration-detail-breadcrumb-export-button'}
              to={this.props.exportHref}
              onClick={this.props.exportAction}
              children={this.props.exportLabel}
            />
            &nbsp;&nbsp;
            <ButtonLink
              data-testid={'integration-detail-breadcrumb-edit-button'}
              className="btn btn-primary"
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
                          data-testid={`integration-detail-breadcrumb-${toValidHtmlId(
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
                          data-testid={`integration-detail-breadcrumb-${toValidHtmlId(
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
                  ))
                : null}
            </DropdownKebab>
          </>
        }
      >
        <span>
          <Link
            data-testid={'integration-detail-breadcrumb-home-link'}
            to={this.props.homeHref!}
          >
            {this.props.i18nHome}
          </Link>
        </span>
        <span>
          <Link
            data-testid={'integration-detail-breadcrumb-integrations-link'}
            to={this.props.integrationsHref!}
          >
            {this.props.i18nIntegrations}
          </Link>
        </span>
        <span>{this.props.i18nPageTitle}</span>
      </Breadcrumb>
    );
  }
}
