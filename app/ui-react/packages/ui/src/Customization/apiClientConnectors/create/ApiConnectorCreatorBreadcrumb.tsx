import * as H from '@syndesis/history';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { Breadcrumb, ButtonLink } from '../../../Layout';

export interface IApiConnectorCreatorBreadcrumbProps {
  i18nCancel: string;
  i18nConnectors: string;
  i18nCreateConnection: string;
  cancelHref: H.LocationDescriptor;
  connectorsHref: H.LocationDescriptor;
}

export const ApiConnectorCreatorBreadcrumb: React.FunctionComponent<IApiConnectorCreatorBreadcrumbProps> = (
  {
    i18nCancel,
    i18nConnectors,
    i18nCreateConnection,
    cancelHref,
    connectorsHref
  }) => (
  <section className={'pf-c-page__main-breadcrumb'}>
    <Breadcrumb
      actions={
        <ButtonLink
          data-testid={'connection-creator-layout-cancel-button'}
          href={cancelHref}
          className={'wizard-pf-cancel'}
        >
          {i18nCancel}
        </ButtonLink>
      }
    >
      <Link
        data-testid={'connections-creator-app-connections-link'}
        to={connectorsHref}
      >
        {i18nConnectors}
      </Link>
      <span>{i18nCreateConnection}</span>
    </Breadcrumb>
  </section>
);
