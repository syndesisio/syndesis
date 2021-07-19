import {
  PageSectionProps,
  Popover,
  Text,
  TextContent,
  Title,
  TitleProps,
} from '@patternfly/react-core';
import { InfoCircleIcon } from '@patternfly/react-icons';
import classnames from 'classnames';
import * as React from 'react';
import { PageSection } from '../Layout';
import './SimplePageHeader.css';

export interface ISimplePageHeaderProps {
  i18nTitle: string;
  i18nDescription?: string;
  variant?: PageSectionProps['variant'];
  titleSize?: TitleProps['size'];
  titleHeadingLevel?: 'h1' | 'h2' | 'h3' | 'h4' | 'h5' | 'h6';
  className?: string;
  isTechPreview?: boolean;
  i18nTechPreview?: string;
  techPreviewPopoverHtml?: React.ReactNode;
}

export const SimplePageHeader: React.FunctionComponent<ISimplePageHeaderProps> =
  ({
    i18nTitle,
    i18nDescription,
    variant = 'light',
    titleSize = '2xl',
    titleHeadingLevel = 'h1',
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
            headingLevel={titleHeadingLevel}
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
                  <InfoCircleIcon className="simple-page-header__tech-preview-icon" />
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
