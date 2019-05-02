import { TextContent, TextList, TextListItem } from '@patternfly/react-core';
import * as React from 'react';

export interface IAboutModalContent {
  version: string;
  productName: string;
  buildId: string;
  commitId: string;
}

function AboutModalContent({
  productName,
  version,
  buildId,
  commitId,
}: IAboutModalContent) {
  return (
    <TextContent>
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
