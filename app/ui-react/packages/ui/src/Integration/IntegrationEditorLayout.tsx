// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import {
  Level,
  LevelItem,
  TextContent,
  Title,
  TitleLevel,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink, Loader, PageSection } from '../Layout';
import { InlineTextEdit, SimplePageHeader } from '../Shared';
import './IntegrationEditorLayout.css';

/**
 * @param header - a PatternFly Wizard Steps component.
 * @param sidebar - the sidebar container takes the size of its content. If no
 * sidebar is defined, a layout with just the header, the footer and the body
 * will be shown.
 * @param content - the main content of the wizard. In case of overflow, only
 * the body will scroll.
 * @param onPublish - if passed, the Cancel button will be render as a `button`
 * and this callback will be used as its `onClick` handler.
 * @param onCancel - if passed, the Back button will be render as a `button`
 * and this callback will be used as its `onClick` handler.
 * @param onSave - if passed, the Next button will be render as a `button`
 * and this callback will be used as its `onClick` handler.
 * @param cancelHref - if passed, the Cancel button will be render as a `Link`
 * using this as its `to` parameter.
 * @param backHref - if passed, the Back button will be render as a `Link`
 * using this as its `to` parameter.
 * using this as its `to` parameter.
 * @param isSaveLoading - if set to true, a `Loading` component will be shown
 * inside the Next button before its label. The button will also be disabled.
 * @param isSaveDisabled - if set to true, the Next button will be disabled.
 * @param isLastStep - if set to true, it changes the Next button label to
 * 'Done'.
 */
export interface IIntegrationEditorLayoutProps {
  integrationName?: string;
  integrationDescription?: string;
  i18nIntegrationNamePlaceholder: string;
  i18nIntegrationDescriptionPlaceholder: string;
  onIntegrationNameChange: (name: string) => Promise<boolean>;
  onIntegrationDescriptionChange: (name: string) => Promise<boolean>;
  title: string;
  description?: string;
  sidebar?: React.ReactNode;
  content: React.ReactNode;
  onCancel?: (e: React.MouseEvent<any>) => void;
  onPublish?: (e: React.MouseEvent<any>) => void;
  onSave?: (e: React.MouseEvent<any>) => void;
  saveHref?: H.LocationDescriptor;
  cancelHref?: H.LocationDescriptor;
  publishHref?: H.LocationDescriptor;
  isSaveDisabled?: boolean;
  isSaveLoading?: boolean;
  isPublishDisabled?: boolean;
  isPublishLoading?: boolean;
}

/**
 * Provides the layout for the integration editor. It uses the PatternFly Wizard
 * component under the hood.
 * The footer is pre-defined and follows the PF Wizard pattern, with
 * Cancel/Previous/Next buttons.
 *
 * @todo in the CSS we use hardcoded values for the heights of various
 * elements of the page to be able to size the element to take all the available
 * height and show the right scrollbars.
 * We should really find a smarter way to handle this.
 */
export const IntegrationEditorLayout: React.FunctionComponent<
  IIntegrationEditorLayoutProps
> = ({
  integrationName,
  integrationDescription,
  i18nIntegrationNamePlaceholder,
  i18nIntegrationDescriptionPlaceholder,
  onIntegrationNameChange,
  onIntegrationDescriptionChange,
  title,
  description,
  sidebar,
  content,
  onPublish,
  onCancel,
  onSave,
  saveHref,
  cancelHref,
  publishHref,
  isSaveLoading,
  isSaveDisabled,
  isPublishLoading,
  isPublishDisabled,
}: IIntegrationEditorLayoutProps) => {
  return (
    <div className={'wizard-pf-body integration-editor-layout syn-scrollable'}>
      <div className="wizard-pf-toolbar integration-editor-layout__header">
        <PageSection variant={'light'}>
          <Level gutter={'sm'}>
            <LevelItem>
              <TextContent>
                <Title size={'2xl'} headingLevel={TitleLevel.h1}>
                  <InlineTextEdit
                    value={integrationName || ''}
                    i18nPlaceholder={i18nIntegrationNamePlaceholder}
                    allowEditing={!isSaveDisabled}
                    isTextArea={false}
                    onChange={onIntegrationNameChange}
                  />
                </Title>
                <InlineTextEdit
                  value={integrationDescription || ''}
                  i18nPlaceholder={i18nIntegrationDescriptionPlaceholder}
                  allowEditing={!isSaveDisabled}
                  isTextArea={false}
                  onChange={onIntegrationDescriptionChange}
                />
              </TextContent>
            </LevelItem>
            <LevelItem>
              {(cancelHref || onCancel) && (
                <ButtonLink onClick={onCancel} href={cancelHref}>
                  <i className="fa fa-angle-left" /> Cancel
                </ButtonLink>
              )}
              {(saveHref || onSave) && (
                <ButtonLink
                  onClick={onSave}
                  href={saveHref}
                  disabled={isSaveLoading || isSaveDisabled}
                >
                  {isSaveLoading ? <Loader size={'xs'} inline={true} /> : null}
                  Save
                </ButtonLink>
              )}
              {(publishHref || onPublish) && (
                <ButtonLink
                  onClick={onPublish}
                  href={publishHref}
                  as={'primary'}
                  disabled={isPublishLoading || isPublishDisabled}
                >
                  Publish
                </ButtonLink>
              )}
            </LevelItem>
          </Level>
        </PageSection>
      </div>
      <div className="wizard-pf-row integration-editor-layout__body syn-scrollable--body">
        <div className="wizard-pf-sidebar">{sidebar}</div>
        <div
          className={
            'wizard-pf-main cards-pf integration-editor-layout__contentWrapper'
          }
        >
          <div className="integration-editor-layout__content">
            <SimplePageHeader
              i18nTitle={title}
              i18nDescription={description}
              className={'integration-editor-layout__header'}
            />
            {content}
          </div>
        </div>
      </div>
    </div>
  );
};
