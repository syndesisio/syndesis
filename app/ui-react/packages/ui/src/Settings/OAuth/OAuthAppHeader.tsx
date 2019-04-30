import { Text, Title, TitleLevel } from '@patternfly/react-core';
import classnames from 'classnames';
import * as React from 'react';
import { Container } from '../../Layout';

export interface IOAuthAppHeaderProps {
  i18nPageTitle: string;
  i18nDescription: string;
  i18nCallbackDescription: string;
  callbackURI: string;
  className?: string;
}

export const OAuthAppHeader: React.FunctionComponent<IOAuthAppHeaderProps> = ({
  i18nPageTitle,
  i18nDescription,
  i18nCallbackDescription,
  callbackURI,
  className,
}) => (
  <Container className={classnames('', className)}>
    <Title size="3xl" headingLevel={TitleLevel.h2}>
      {i18nPageTitle}
    </Title>
    <Text dangerouslySetInnerHTML={{ __html: i18nDescription }} />
    <Text>
      {i18nCallbackDescription} : {callbackURI}
    </Text>
  </Container>
);
