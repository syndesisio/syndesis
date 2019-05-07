import * as React from 'react';
import { AboutModalContent } from '../Shared';

export interface ISupportPageBodyProps {
  i18nProductName: string;
  version: string;
  buildId: string;
  commitId: string;
}

export const SupportPageBody: React.FunctionComponent<
  ISupportPageBodyProps
> = ({ i18nProductName, version, buildId, commitId }) => (
  <>
    <AboutModalContent
      className="pf-u-mx-xl pf-u-my-xl"
      version={version}
      buildId={buildId}
      commitId={commitId}
      productName={i18nProductName}
    />
  </>
);
