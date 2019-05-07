import { WithApiVersion } from '@syndesis/api';
import { PageSection, SimplePageHeader, SupportPageBody } from '@syndesis/ui';
import * as React from 'react';
import { PageTitle } from '../../../shared';

export function SupportPage() {
  return (
    <>
      <PageTitle title="Support" />
      <SimplePageHeader
        i18nTitle={'Support'}
        i18nDescription={`To obtain support, download diagnostic information through this page and open a request on the <a href="https://access.redhat.com/support/cases/#/case/new">Red Hat Customer portal</a>. If you have any issues please see the support <a href="https://access.redhat.com/solutions/2112">instructions</a>.`}
      />
      <PageSection>
        <h2>Version</h2>
        <WithApiVersion>
          {({ data }) => {
            const {
              'commit-id': commitId,
              'build-id': buildId,
              version,
            } = data;
            return (
              <SupportPageBody
                i18nProductName={'Syndesis'}
                version={version}
                buildId={buildId}
                commitId={commitId}
              />
            );
          }}
        </WithApiVersion>
      </PageSection>
    </>
  );
}
