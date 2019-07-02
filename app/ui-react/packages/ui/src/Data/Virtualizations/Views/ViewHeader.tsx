import { Text, TextContent, Title, TitleLevel } from '@patternfly/react-core';
import * as React from 'react';
import { PageSection } from '../../../Layout';

export interface IViewHeaderProps {
  i18nTitle: string;
  i18nDescription: string;
}

export const ViewHeader: React.FunctionComponent<
  IViewHeaderProps
> = props => {
  return (
    <PageSection variant={'light'}>
      <TextContent>
        <Title size="2xl" headingLevel={TitleLevel.h1}>
          {props.i18nTitle}
        </Title>
        {props.i18nDescription && (
          <Text>{props.i18nDescription}</Text>
        )}
      </TextContent>
    </PageSection>
  );
}
