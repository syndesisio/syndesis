import * as H from '@syndesis/history';
import { Breadcrumb, ButtonLink } from '@syndesis/ui';
import * as React from 'react';
import { Link } from 'react-router-dom';
import resolvers from '../resolvers';

export interface IApiConnectorCreatorBreadcrumbProps {
  cancelHref: H.LocationDescriptor;
}
export const ApiConnectorCreatorBreadcrumb: React.FunctionComponent<
  IApiConnectorCreatorBreadcrumbProps
> = ({ cancelHref }) => (
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
      to={resolvers.list()}
    >
      API Client Connectors
    </Link>
    <span>Create connection</span>
  </Breadcrumb>
);
