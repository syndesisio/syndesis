import * as H from '@syndesis/history';
import React from 'react';
import { Link } from 'react-router-dom';
import { Breadcrumb } from '../../Layout';

export interface IVirtualizationBreadcrumbProps {
  /**
   * The buttons and kebab menu.
   */
  actions?: JSX.Element;

  /**
   * The HREF of the data virtualization landing page.
   */
  dataPageHref: H.LocationDescriptor;

  /**
   * The Syndesis home page HREF.
   */
  homePageHref: H.LocationDescriptor;

  /**
   * The localized text of the data virtualization landing page.
   */
  i18nDataPageTitle: string;

  /**
   * The localized text of the Syndesis home page.
   */
  i18nHomePageTitle: string;

  /**
   * The localized text of the named virtualization
   */
  i18nVirtualizationBreadcrumb: string;
}

export const VirtualizationBreadcrumb: React.FunctionComponent<
  IVirtualizationBreadcrumbProps
> = props => {
  return (
    <Breadcrumb actions={props.actions}>
      <Link
        data-testid={'virtualization-breadcrumb-home-link'}
        to={props.homePageHref}
      >
        {props.i18nHomePageTitle}
      </Link>
      <Link
        data-testid={'virtualization-breadcrumb-data-link'}
        to={props.dataPageHref}
      >
        {props.i18nDataPageTitle}
      </Link>
      <span>{props.i18nVirtualizationBreadcrumb}</span>
    </Breadcrumb>
  );
};
