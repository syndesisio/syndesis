// import { Split, SplitItem } from '@patternfly/react-core';
import * as React from 'react';
import ContentLoader from 'react-content-loader';

export const SqlClientContentSkeleton: React.FunctionComponent<{}> = props => {
  const inputWidth = 200;
  const rectHeight = 20;
  const spacing = 30;
  const startY = 50;
  const xPos = 30;
  let yPos = startY;

  const colWidth = 75;
  const gap = 1;
  const rowHeight = 25;
  const tableX = xPos + inputWidth + 40;
  const tableY = 25;

  return (
    <ContentLoader
      height={600}
      width={800}
      speed={2}
      primaryColor="#f3f3f3"
      secondaryColor="#ecebeb"
      {...props}
    >
      <rect x={xPos} y={yPos} width={50} height={rectHeight} />
      {(yPos = yPos + spacing)}
      <rect x={xPos} y={yPos} width={inputWidth} height={rectHeight} />
      {(yPos = yPos + spacing)}
      <rect x={xPos} y={yPos} width={60} height={rectHeight} />
      {(yPos = yPos + spacing)}
      <rect x={xPos} y={yPos} width={inputWidth} height={rectHeight} />
      {(yPos = yPos + spacing)}
      <rect x={xPos} y={yPos} width={40} height={rectHeight} />
      {(yPos = yPos + spacing)}
      <rect x={xPos} y={yPos} width={inputWidth} height={rectHeight} />
      {(yPos = yPos + spacing)}
      // border left
      <rect x={xPos - 5} y={startY} width={1} height={yPos - startY} />
      // border bottom
      <rect x={xPos - 5} y={yPos} width={5 + inputWidth + 5} height={1} />
      // border right
      <rect
        x={inputWidth + xPos + 5}
        y={startY}
        width={1}
        height={yPos - startY}
      />
      // button
      {(yPos = yPos + 10)}
      <rect x={xPos + 10} y={yPos} width={40} height={20} />
      // table
      {[
        tableY,
        tableY + rowHeight + gap,
        tableY + 2 * (rowHeight + gap),
        tableY + 3 * (rowHeight + gap),
        tableY + 4 * (rowHeight + gap),
        tableY + 5 * (rowHeight + gap),
        tableY + 6 * (rowHeight + gap),
        tableY + 7 * (rowHeight + gap),
      ].map((y: number) => {
        return (
          // create columns for each row
          <React.Fragment key={y}>
            {[0, 1, 2, 3, 4, 5].map((colNum: number) => {
              return (
                <rect
                  key={y + ':' + colNum}
                  x={tableX + colNum * (colWidth + gap)}
                  y={y}
                  width={colWidth}
                  height={rowHeight}
                />
              );
            })}
          </React.Fragment>
        );
      })}
    </ContentLoader>
  );
};
