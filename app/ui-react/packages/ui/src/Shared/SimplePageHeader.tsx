import { Text, Title, TitleLevel } from '@patternfly/react-core';
import classnames from 'classnames';
import * as React from 'react';
import { Container } from '../Layout';

export interface ISimplePageHeaderProps {
  i18nTitle: string;
  i18nDescription: string;
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
    <Container className={classnames('', className)} {...rest}>
      <Title size="3xl" headingLevel={TitleLevel.h2}>
        {i18nTitle}
      </Title>
      <Text>{i18nDescription}</Text>
    </Container>
  );
};
