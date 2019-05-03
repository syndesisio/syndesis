import { Text, TextContent, Title, TitleLevel } from '@patternfly/react-core';
import classnames from 'classnames';
import * as React from 'react';
import { PageSection } from '../Layout';

export interface ISimplePageHeaderProps {
  i18nTitle: string;
  i18nDescription?: string;
  className?: string;
}

export const SimplePageHeader: React.FunctionComponent<
  ISimplePageHeaderProps
> = ({
  i18nTitle,
  i18nDescription,
  className,
  ...rest
}: ISimplePageHeaderProps) => {
  return (
    <PageSection
      variant={'light'}
      className={classnames('', className)}
      {...rest}
    >
      <TextContent>
        <Title size="2xl" headingLevel={TitleLevel.h1}>
          {i18nTitle}
        </Title>
        {i18nDescription && <Text>{i18nDescription}</Text>}
      </TextContent>
    </PageSection>
  );
};
