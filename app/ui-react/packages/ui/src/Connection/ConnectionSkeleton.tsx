import { Card, CardBody } from '@patternfly/react-core';
import * as React from 'react';
import ContentLoader from 'react-content-loader';

export const ConnectionSkeleton = (props: any) => (
  <Card>
    <CardBody>
      <ContentLoader
        height={250}
        width={360}
        speed={2}
        primaryColor="#f3f3f3"
        secondaryColor="#ecebeb"
        {...props}
      >
        <circle cx="180" cy="65" r="48" />
        <rect x="30" y="150" rx="5" ry="5" width="300" height="20" />
        <rect x="15" y="180" rx="5" ry="5" width="330" height="18" />
      </ContentLoader>
    </CardBody>
  </Card>
);
