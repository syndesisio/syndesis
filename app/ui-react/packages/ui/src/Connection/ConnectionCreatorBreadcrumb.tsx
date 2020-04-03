import * as H from '@syndesis/history';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { Breadcrumb, ButtonLink } from '../Layout';

export interface IConnectionCreatorBreadcrumbProps {
  cancelHref?: H.LocationDescriptor;
  connectionsHref: H.LocationDescriptor;
  i18nCancel: string;
  i18nConnections: string;
  i18nCreateConnection: string;
}

export const ConnectionCreatorBreadcrumb: React.FunctionComponent<IConnectionCreatorBreadcrumbProps> = ({
  cancelHref,
  connectionsHref,
  i18nCancel,
  i18nConnections,
  i18nCreateConnection,
}) => (
  <section className={'pf-c-page__main-breadcrumb'}>
    <Breadcrumb
      actions={
        <>
          {cancelHref ? (
            <ButtonLink
              data-testid={'connection-creator-layout-cancel-button'}
              href={cancelHref}
              className={'wizard-pf-cancel'}
            >
              {i18nCancel}
            </ButtonLink>
          ) : null}
        </>
      }
    >
      <Link
        data-testid={'connections-creator-app-connections-link'}
        to={connectionsHref}
      >
        {i18nConnections}
      </Link>
      <span>{i18nCreateConnection}</span>
    </Breadcrumb>
  </section>
);
