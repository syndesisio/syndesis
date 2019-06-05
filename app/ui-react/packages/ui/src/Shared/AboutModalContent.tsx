import { TextContent, TextList, TextListItem } from '@patternfly/react-core';
import classNames from 'classnames';
import * as React from 'react';

export interface IAboutModalContent {
  className?: string;
  version: string;
  productName: string;
  buildId: string;
  commitId: string;
  i18nBuildIdLabel: string;
  i18nCommitIdLabel: string;
}

function AboutModalContent({
  className,
  productName,
  version,
  buildId,
  commitId,
  i18nBuildIdLabel,
  i18nCommitIdLabel,
}: IAboutModalContent) {
  return (
    <TextContent className={classNames('', className)}>
      <TextList component="dl">
        <TextListItem
          component="dt"
          data-testid={'about-modal-content-product-name-list-item'}
        >
          {productName}:
        </TextListItem>
        <TextListItem
          component="dd"
          data-testid={'about-modal-content-version-list-item'}
        >
          {version}
        </TextListItem>
        <TextListItem component="dt">{i18nBuildIdLabel}</TextListItem>
        <TextListItem
          component="dd"
          data-testid={'about-modal-content-build-id-list-item'}
        >
          {buildId}
        </TextListItem>
        <TextListItem component="dt">{i18nCommitIdLabel}</TextListItem>
        <TextListItem
          component="dd"
          data-testid={'about-modal-content-commit-id-list-item'}
        >
          {commitId}
        </TextListItem>
      </TextList>
    </TextContent>
  );
}

export { AboutModalContent };
