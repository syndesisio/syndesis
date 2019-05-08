import {
  PageSectionProps,
  Text,
  TextContent,
  Title,
  TitleLevel,
  TitleProps,
} from '@patternfly/react-core';
import classnames from 'classnames';
import * as React from 'react';
import { PageSection } from '../Layout';

export interface ISimplePageHeaderProps {
  i18nTitle: string;
  i18nDescription?: string;
  variant?: PageSectionProps['variant'];
  titleSize?: TitleProps['size'];
  titleHeadingLevel?: keyof typeof TitleLevel;
  className?: string;
}

export const SimplePageHeader: React.FunctionComponent<
  ISimplePageHeaderProps
> = ({
  i18nTitle,
  i18nDescription,
  variant = 'light',
  titleSize = '2xl',
  titleHeadingLevel = TitleLevel.h1,
  className,
  ...rest
}: ISimplePageHeaderProps) => {
  return (
    <PageSection
      variant={variant}
      className={classnames('', className)}
      {...rest}
    >
      <TextContent>
        <Title size={titleSize} headingLevel={TitleLevel[titleHeadingLevel]}>
          {i18nTitle}
        </Title>
        {i18nDescription && (
          <Text dangerouslySetInnerHTML={{ __html: i18nDescription }} />
        )}
      </TextContent>
    </PageSection>
  );
};
