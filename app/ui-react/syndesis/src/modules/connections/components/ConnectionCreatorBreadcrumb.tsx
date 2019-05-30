import * as H from '@syndesis/history';
import { Breadcrumb, ButtonLink } from '@syndesis/ui';
import * as React from 'react';
import { Link } from 'react-router-dom';
import resolvers from '../resolvers';

export interface IConnectionCreatorBreadcrumbProps {
  cancelHref: H.LocationDescriptor;
}
export const ConnectionCreatorBreadcrumb: React.FunctionComponent<
  IConnectionCreatorBreadcrumbProps
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
      to={resolvers.connections()}
    >
      Connections
    </Link>
    <span>Create connection</span>
  </Breadcrumb>
);
