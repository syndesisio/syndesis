import * as React from 'react';
import ContentLoader from 'react-content-loader';

function getRandom(min: number, max: number) {
  return Math.random() * (max - min) + min;
}

const ItemSkeleton = () => (
  <ContentLoader
    height={80}
    width={500}
    speed={2}
    primaryColor="#f3f3f3"
    secondaryColor="#ecebeb"
    style={{
      height: 80,
      width: '100%',
    }}
  >
    <circle cx="35" cy="40" r="35" />
    <circle cx="115" cy="40" r="35" />
    <rect
      x="185"
      y="20"
      rx="5"
      ry="5"
      width={400 * getRandom(0.6, 1)}
      height="18"
    />
    <rect
      x="185"
      y="55"
      rx="5"
      ry="5"
      width={200 * getRandom(0.6, 1)}
      height="15"
    />
  </ContentLoader>
);

export class IntegrationsListSkeleton extends React.PureComponent {
  public render() {
    return (
      <>
        <div className={'list-group-item'}>
          <div>
            <ItemSkeleton />
          </div>
        </div>
        <div className={'list-group-item'}>
          <div>
            <ItemSkeleton />
          </div>
        </div>
        <div className={'list-group-item'}>
          <div>
            <ItemSkeleton />
          </div>
        </div>
      </>
    );
  }
}
