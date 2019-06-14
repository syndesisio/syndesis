import {
  TextContent,
  TextList,
  TextListItem,
  TextListItemVariants,
  TextListVariants,
} from '@patternfly/react-core';
import * as React from 'react';
import { PageSection } from '../../Layout';
import { CopyToClipboard } from '../../Shared/CopyToClipboard';
import './IntegrationExposedURL.css';

export interface IIntegrationExposedURLProps {
  url?: string;
}

export const IntegrationExposedURL: React.FunctionComponent<
  IIntegrationExposedURLProps
> = ({ url }) => (
  <>
    {url && (
      <PageSection>
        <TextContent>
          <TextList
            component={TextListVariants.dl}
            className="integration-exposed-url__list"
          >
            <TextListItem component={TextListItemVariants.dt}>
              External URL
            </TextListItem>
            <TextListItem component={TextListItemVariants.dd}>
              <CopyToClipboard>{url}</CopyToClipboard>
            </TextListItem>
          </TextList>
        </TextContent>
      </PageSection>
    )}
  </>
);
