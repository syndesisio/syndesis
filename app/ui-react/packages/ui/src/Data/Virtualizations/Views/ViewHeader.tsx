import { Text, TextContent, Title, TitleLevel } from '@patternfly/react-core';
import * as React from 'react';
import { PageSection } from '../../../Layout';

export interface IViewHeaderProps {
  i18nTitle: string;
  i18nDescription: string;
}

export class ViewHeader extends React.Component<IViewHeaderProps> {
  public render() {
    return (
      <PageSection variant={'light'}>
        <TextContent>
          <Title size="2xl" headingLevel={TitleLevel.h1}>
            {this.props.i18nTitle}
          </Title>
          {this.props.i18nDescription && (
            <Text>{this.props.i18nDescription}</Text>
          )}
        </TextContent>
      </PageSection>
    );
  }
}
