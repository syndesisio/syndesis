import * as React from 'react';
import { PageSection } from '../../Layout';
import { CopyToClipboard } from '../../Shared/CopyToClipboard';

export interface IIntegrationExposedURLProps {
  url?: string;
}

export const IntegrationExposedURL: React.FunctionComponent<
  IIntegrationExposedURLProps
> = ({ url }) => (
  <>
    {url && (
      <PageSection>
        <CopyToClipboard>{url}</CopyToClipboard>
      </PageSection>
    )}
  </>
);
