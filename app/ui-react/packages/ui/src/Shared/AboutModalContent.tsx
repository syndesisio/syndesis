import { TextContent, TextList, TextListItem } from '@patternfly/react-core';
import classNames from 'classnames';
import * as React from 'react';

export interface IAboutModalContent {
  className?: string;
  version: string;
  productName: string;
  buildId: string;
  commitId: string;
}

function AboutModalContent({
  className,
  productName,
  version,
  buildId,
  commitId,
}: IAboutModalContent) {
  return (
    <TextContent className={classNames('', className)}>
      <TextList component="dl">
        <TextListItem component="dt">{productName}:</TextListItem>
        <TextListItem component="dd">{version}</TextListItem>
        <TextListItem component="dt">Build ID:</TextListItem>
        <TextListItem component="dd">{buildId}</TextListItem>
        <TextListItem component="dt">Commit ID:</TextListItem>
        <TextListItem component="dd">{commitId}</TextListItem>
      </TextList>
    </TextContent>
  );
}

export { AboutModalContent };
