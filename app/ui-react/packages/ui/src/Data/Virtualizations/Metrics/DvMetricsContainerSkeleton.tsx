import {
  Card,
  CardBody,
  Flex,
  FlexItem,
} from '@patternfly/react-core';
import * as React from 'react';
import ContentLoader from 'react-content-loader';
import './DvMetricsContainer.css';

export const DvMetricsContainerSkeleton = () => (
  <Flex
    breakpointMods={[{ modifier: 'space-items-xl', breakpoint: 'xl' }]}
    className={'dv-metrics-container__flexAlign'}
  >
    {new Array(4).fill(0).map((_, index) => (
      <FlexItem
        key={index}
        breakpointMods={[{ modifier: 'flex-1', breakpoint: 'xl' }]}
      >
        <Card className="dv-metrics-container__card">
          <CardBody>
            <ContentLoader
              height={150}
              width={150}
              speed={2}
              primaryColor="#f3f3f3"
              secondaryColor="#ecebeb"
            >
              <rect x="0" y="0" width="100" height="20" />
            </ContentLoader>
          </CardBody>
        </Card>
      </FlexItem>
    ))}
  </Flex>
);
