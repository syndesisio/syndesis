import * as React from 'react';
import ContentLoader from 'react-content-loader';

export const RecentUpdatesSkeleton = (props: any) => (
  <ContentLoader
    height={150}
    width={350}
    speed={2}
    primaryColor="#f3f3f3"
    secondaryColor="#ecebeb"
    {...props}
  >
    <rect x="20" y="5" rx="5" ry="5" width="100" height="10" />
    <rect x="150" y="5" rx="5" ry="5" width="30" height="10" />
    <rect x="250" y="5" rx="5" ry="5" width="40" height="10" />

    <rect x="20" y="35" rx="5" ry="5" width="80" height="10" />
    <rect x="150" y="35" rx="5" ry="5" width="30" height="10" />
    <rect x="250" y="35" rx="5" ry="5" width="40" height="10" />

    <rect x="20" y="65" rx="5" ry="5" width="95" height="10" />
    <rect x="150" y="65" rx="5" ry="5" width="30" height="10" />
    <rect x="250" y="65" rx="5" ry="5" width="40" height="10" />

    <rect x="20" y="95" rx="5" ry="5" width="125" height="10" />
    <rect x="150" y="95" rx="5" ry="5" width="30" height="10" />
    <rect x="250" y="95" rx="5" ry="5" width="40" height="10" />

    <rect x="20" y="125" rx="5" ry="5" width="65" height="10" />
    <rect x="150" y="125" rx="5" ry="5" width="30" height="10" />
    <rect x="250" y="125" rx="5" ry="5" width="40" height="10" />
  </ContentLoader>
);
