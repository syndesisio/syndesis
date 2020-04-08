import * as H from '@syndesis/history';
import { Breadcrumb, ButtonLink } from '@syndesis/ui';
import * as React from 'react';
import { Link } from 'react-router-dom';

export interface IApiConnectorCreatorBreadcrumbProps {
  cancelHref: H.LocationDescriptor;
  connectorsHref: H.LocationDescriptor;
}
export const ApiConnectorCreatorBreadcrumb: React.FunctionComponent<
  IApiConnectorCreatorBreadcrumbProps
> = (
  {
    cancelHref,
    connectorsHref
  }) => (
  <Breadcrumb
    actions={
      <ButtonLink
        data-testid={'connection-creator-layout-cancel-button'}
        href={cancelHref}
        className={'wizard-pf-cancel'}
      >
        Cancel
      </ButtonLink>
    }
  >
    <Link
      data-testid={'connections-creator-app-connections-link'}
      to={connectorsHref}
    >
      API Client Connectors
    </Link>
    <span>Create connection</span>
  </Breadcrumb>
);
