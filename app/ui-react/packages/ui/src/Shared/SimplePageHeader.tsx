import * as React from 'react';
import { Container } from '../Layout';

export interface ISimplePageHeaderProps {
  i18nTitle: string;
  i18nDescription: string;
}

export class SimplePageHeader extends React.Component<ISimplePageHeaderProps> {
  public render() {
    return (
      <Container>
        <h2>{this.props.i18nTitle}</h2>
        <h3>{this.props.i18nDescription}</h3>
      </Container>
    );
  }
}
