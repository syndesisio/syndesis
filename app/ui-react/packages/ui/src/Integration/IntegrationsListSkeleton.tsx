import * as React from 'react';
import ContentLoader from 'react-content-loader';
import { toTestId } from '../utils';

function getRandom(min: number, max: number) {
  return Math.random() * (max - min) + min;
}

const ItemSkeleton = () => (
  <ContentLoader
    height={80}
    width={400}
    speed={2}
    primaryColor="#f3f3f3"
    secondaryColor="#ecebeb"
    style={{
      height: 80,
    }}
  >
    <circle cx="35" cy="40" r="25" />
    <circle cx="95" cy="40" r="25" />
    <rect
      x="145"
      y="20"
      rx="5"
      ry="5"
      width={350 * getRandom(0.6, 1)}
      height="18"
    />
    <rect
      x="145"
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
        <div
          className={'list-group-item'}
          data-testid={`${toTestId('IntegrationsListSkeleton', 'item-0')}`}
        >
          <div>
            <ItemSkeleton />
          </div>
        </div>
        <div
          className={'list-group-item'}
          data-testid={`${toTestId('IntegrationsListSkeleton', 'item-1')}`}
        >
          <div>
            <ItemSkeleton />
          </div>
        </div>
        <div
          className={'list-group-item'}
          data-testid={`${toTestId('IntegrationsListSkeleton', 'item-2')}`}
        >
          <div>
            <ItemSkeleton />
          </div>
        </div>
      </>
    );
  }
}
