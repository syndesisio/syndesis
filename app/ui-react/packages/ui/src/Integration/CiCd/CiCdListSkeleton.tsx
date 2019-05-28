import * as React from 'react';
import ContentLoader from 'react-content-loader';

function getRandom(min: number, max: number) {
  return Math.random() * (max - min) + min;
}

const ItemSkeleton = () => (
  <ContentLoader
    height={68}
    width={500}
    speed={2}
    primaryColor="#f3f3f3"
    secondaryColor="#ecebeb"
    style={{
      height: 68,
      width: '100%',
    }}
  >
    <rect
      x="15"
      y="20"
      rx="5"
      ry="5"
      width={400 * getRandom(0.6, 1)}
      height="18"
    />
  </ContentLoader>
);

export class CiCdListSkeleton extends React.PureComponent {
  public render() {
    return (
      <>
        <div
          className={'list-group-item'}
          data-testid={'cicd-list-skeleton-item-0'}
        >
          <div>
            <ItemSkeleton />
          </div>
        </div>
        <div
          className={'list-group-item'}
          data-testid={'cicd-list-skeleton-item-1'}
        >
          <div>
            <ItemSkeleton />
          </div>
        </div>
        <div
          className={'list-group-item'}
          data-testid={'cicd-list-skeleton-item-2'}
        >
          <div>
            <ItemSkeleton />
          </div>
        </div>
      </>
    );
  }
}
