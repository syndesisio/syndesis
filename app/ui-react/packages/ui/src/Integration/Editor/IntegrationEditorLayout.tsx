// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import {
  Level,
  LevelItem,
  Text,
  TextContent,
  Title,
  TitleLevel,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink, Loader, PageSection } from '../../Layout';
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
  title: string;
  description?: string;
  toolbar: React.ReactNode;
  sidebar?: React.ReactNode;
  content: React.ReactNode;
  onCancel?: (e: React.MouseEvent<any>) => void;
  onPublish?: (e: React.MouseEvent<any>) => void;
  onSave?: (e: React.MouseEvent<any>) => void;
  saveHref?: H.LocationDescriptor;
  cancelHref?: H.LocationDescriptor;
  publishHref?: H.LocationDescriptor;
  primaryFlowHref?: H.LocationDescriptor;
  isApiProvider?: boolean;
  isAlternateFlow?: boolean;
  isDefaultFlow?: boolean;
  isSaveDisabled?: boolean;
  isSaveLoading?: boolean;
  isPublishDisabled?: boolean;
  isPublishLoading?: boolean;
  extraActions?: React.ReactNode;
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
  title,
  description,
  toolbar,
  sidebar,
  content,
  onPublish,
  onCancel,
  onSave,
  saveHref,
  cancelHref,
  publishHref,
  primaryFlowHref,
  isApiProvider,
  isAlternateFlow,
  isDefaultFlow,
  isSaveLoading,
  isSaveDisabled,
  isPublishLoading,
  isPublishDisabled,
  extraActions,
}: IIntegrationEditorLayoutProps) => {
  const condition = isDefaultFlow ? 'OTHERWISE' : 'WHEN';

  return (
    <div className={'integration-editor-layout'}>
      <div className={'integration-editor-layout__header'}>
        <PageSection variant={'light'}>
          {toolbar}
          <Level gutter={'sm'}>
            {isAlternateFlow ? (
            <>
              <LevelItem>
                <TextContent>
                  <Title size={'2xl'} headingLevel={TitleLevel.h1}>
                    <strong
                      className="integration-editor-condition"
                      data-verb={condition}
                    >
                      {condition}
                    </strong>
                    &nbsp;{title}
                  </Title>
                  <Text>{description}</Text>
                </TextContent>
              </LevelItem>
              <LevelItem>
                <ButtonLink
                  id={'integration-editor-back-button'}
                  href={primaryFlowHref}
                >
                  {isApiProvider ?
                    'Go to Operation Flow' :
                    'Go to Primary Flow'
                  }
                </ButtonLink>
              </LevelItem>
            </>
            ) : (
            <>
              <LevelItem>
                <TextContent>
                  <Title size={'2xl'} headingLevel={TitleLevel.h1}>
                    {title}
                  </Title>
                  <Text>{description}</Text>
                </TextContent>
              </LevelItem>
              <LevelItem>
                {(cancelHref || onCancel) && (
                  <>
                    <ButtonLink
                      id={'integration-editor-cancel-button'}
                      onClick={onCancel}
                      href={cancelHref}
                    >
                      Cancel
                    </ButtonLink>
                    &nbsp;&nbsp;&nbsp;
                  </>
                )}
                {(saveHref || onSave) && (
                  <>
                    <ButtonLink
                      id={'integration-editor-save-button'}
                      onClick={onSave}
                      href={saveHref}
                      disabled={isSaveLoading || isSaveDisabled}
                      as={publishHref || onPublish ? 'default' : 'primary'}
                    >
                      {isSaveLoading ? (
                        <Loader size={'xs'} inline={true} />
                      ) : null}
                      Save
                    </ButtonLink>
                    &nbsp;
                  </>
                )}
                {(publishHref || onPublish) && (
                  <ButtonLink
                    id={'integration-editor-publish-button'}
                    onClick={onPublish}
                    href={publishHref}
                    as={'primary'}
                    disabled={isPublishLoading || isPublishDisabled}
                  >
                    Publish
                  </ButtonLink>
                )}
                {extraActions}
              </LevelItem>
            </>
            )}
          </Level>
        </PageSection>
      </div>
      <div className={'integration-editor-layout__body'}>
        <div className={'integration-editor-layout__sidebarOuter'}>
          <div className={'integration-editor-layout__sidebarInner'}>
            {sidebar}
          </div>
        </div>
        <div className={'integration-editor-layout__contentOuter'}>
          <div className={'integration-editor-layout__contentInner'}>
            {content}
          </div>
        </div>
      </div>
    </div>
  );
};
