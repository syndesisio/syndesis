import { Card, CardBody } from '@patternfly/react-core';
import * as React from 'react';
import ContentLoader from 'react-content-loader';

export const DvConnectionSkeleton = (props: any) => (
  <Card>
    <CardBody>
      <ContentLoader
        height={150}
        width={150}
        speed={2}
        primaryColor="#f3f3f3"
        secondaryColor="#ecebeb"
        {...props}
      >
        <rect x="5" y="5" width="50" height="25" />
        <circle cx="75" cy="65" r="25" />
        <rect x="25" y="100" width="100" height="20" />
        <rect x="15" y="130" width="120" height="15" />
      </ContentLoader>
    </CardBody>
  </Card>
);
