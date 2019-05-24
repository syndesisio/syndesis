import * as React from 'react';
import { Container, Loader, PageSection } from '../../../Layout';

export class PageSectionLoader extends React.Component {
  public render() {
    return (
      <PageSection>
        <Container>
          <Loader size="lg" />
        </Container>
      </PageSection>
    );
  }
}
