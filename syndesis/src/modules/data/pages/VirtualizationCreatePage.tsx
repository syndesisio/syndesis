import { Breadcrumb, Container } from '@syndesis/ui';
import * as React from 'react';
import { Link } from 'react-router-dom';
import resolvers from '../resolvers';

export default class VirtualizationCreatePage extends React.Component {
  public render() {
    return (
      <>
        <Breadcrumb>
          <Link to={resolvers.root()}>Data Virtualizations</Link>
          <span>Create Virtualization</span>
        </Breadcrumb>
        <Container>
          <h1>Create Virtualization</h1>
          <p>Components for creating the virtualization go here</p>
        </Container>
      </>
    );
  }
}
