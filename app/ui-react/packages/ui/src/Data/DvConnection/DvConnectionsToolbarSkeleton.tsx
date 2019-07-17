import * as React from 'react';
import ContentLoader from 'react-content-loader';

export const DvConnectionsToolbarSkeleton = () => (
  <div>
    <ContentLoader
      height={50}
      width={500}
      speed={2}
      primaryColor="#ffffff"
      secondaryColor="#ecebeb"
    >
      <rect x="0" y="0" height="25" width="500" />
    </ContentLoader>
  </div>
);
