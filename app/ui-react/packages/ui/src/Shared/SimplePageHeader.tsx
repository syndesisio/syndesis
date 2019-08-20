import {
  PageSectionProps,
  Popover,
  Text,
  TextContent,
  Title,
  TitleLevel,
  TitleProps,
} from '@patternfly/react-core';
import classnames from 'classnames';
import { Icon } from 'patternfly-react';
import * as React from 'react';
import { PageSection } from '../Layout';
import './SimplePageHeader.css';

export interface ISimplePageHeaderProps {
  i18nTitle: string;
  i18nDescription?: string;
  variant?: PageSectionProps['variant'];
  titleSize?: TitleProps['size'];
  titleHeadingLevel?: keyof typeof TitleLevel;
  className?: string;
  isTechPreview?: boolean;
  i18nTechPreview?: string;
  techPreviewPopoverHtml?: React.ReactNode;
}

export const SimplePageHeader: React.FunctionComponent<
  ISimplePageHeaderProps
> = ({
  i18nTitle,
  i18nDescription,
  variant = 'light',
  titleSize = '2xl',
  titleHeadingLevel = TitleLevel.h1,
  className,
  isTechPreview,
  i18nTechPreview,
  techPreviewPopoverHtml,
  ...rest
}: ISimplePageHeaderProps) => {
  return (
    <PageSection
      variant={variant}
      className={classnames('', className)}
      {...rest}
    >
      <TextContent>
        <Title
          size={titleSize}
          headingLevel={TitleLevel[titleHeadingLevel]}
          className={
            'simple-page-header__title ' +
            (isTechPreview ? 'simple-page-header__title_tech-preview' : '')
          }
          data-testid={'simple-page-header-title'}
        >
          <span className="simple-page-header__title-text">{i18nTitle}</span>
          {isTechPreview && (
            <span className="simple-page-header__tech-preview-text">
              {i18nTechPreview}
              <Popover
                bodyContent={
                  <React.Fragment>{techPreviewPopoverHtml}</React.Fragment>
                }
                aria-label={i18nTechPreview}
                position={'bottom'}
              >
                <Icon
                  type={'pf'}
                  name={'info'}
                  className="simple-page-header__tech-preview-icon"
                />
              </Popover>
            </span>
          )}
        </Title>
        {i18nDescription && (
          <Text
            className={'simple-page-header__description'}
            dangerouslySetInnerHTML={{ __html: i18nDescription }}
          />
        )}
      </TextContent>
    </PageSection>
  );
};
