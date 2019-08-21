import { Text, TextContent, Title, TitleLevel } from '@patternfly/react-core';
import * as React from 'react';
import { PageSection } from '../../Layout';
import { CopyToClipboard } from '../../Shared/CopyToClipboard';

export interface IOAuthAppHeaderProps {
  i18nPageTitle: string;
  i18nDescription: string;
  i18nCallbackDescription: string;
  callbackURI: string;
}

export const OAuthAppHeader: React.FunctionComponent<IOAuthAppHeaderProps> = ({
  i18nPageTitle,
  i18nDescription,
  i18nCallbackDescription,
  callbackURI,
}) => (
  <PageSection variant={'light'}>
    <TextContent>
      <Title
        size={'2xl'}
        headingLevel={TitleLevel.h1}
        data-testid={'oauth-title'}
      >
        {i18nPageTitle}
      </Title>
      <Text dangerouslySetInnerHTML={{ __html: i18nDescription }} />
      <div>
        <span className="pf-u-mr-md">{i18nCallbackDescription}:</span>
        <CopyToClipboard>{callbackURI}</CopyToClipboard>
      </div>
    </TextContent>
  </PageSection>
);
