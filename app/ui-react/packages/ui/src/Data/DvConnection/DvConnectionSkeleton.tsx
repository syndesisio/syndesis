import { Card, EmptyState } from 'patternfly-react';
import * as React from 'react';
import ContentLoader from 'react-content-loader';

export const DvConnectionSkeleton = (props: any) => (
  <Card matchHeight={true}>
    <Card.Body>
      <EmptyState>
        <ContentLoader
          height={300}
          width={200}
          speed={2}
          primaryColor="#f3f3f3"
          secondaryColor="#ecebeb"
          {...props}
        >
          <circle cx="100" cy="50" r="40" />
          <rect x="5" y="125" rx="5" ry="5" width="190" height="30" />
          <rect x="25" y="180" rx="5" ry="5" width="150" height="15" />
          <rect x="40" y="205" rx="5" ry="5" width="120" height="15" />
        </ContentLoader>
      </EmptyState>
    </Card.Body>
  </Card>
);
