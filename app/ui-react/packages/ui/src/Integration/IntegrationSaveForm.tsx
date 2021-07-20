import { Card, CardBody, CardFooter , Form, Title, CardTitle  } from '@patternfly/react-core';
import * as React from 'react';
import { ButtonLink, Loader, PageSection } from '../Layout';

export interface IIntegrationSaveFormProps {
  /**
   * The internationalized form title.
   */
  i18nFormTitle?: string;

  /**
   * The callback fired when submitting the form.
   * @param e
   */
  handleSubmit: (e?: any) => void;

  onPublish: (e: React.MouseEvent<any>) => void;
  onSave: (e: React.MouseEvent<any>) => void;
  isSaveDisabled: boolean;
  isSaveLoading: boolean;
  isPublishDisabled: boolean;
  isPublishLoading: boolean;
  i18nSave: string;
  i18nSaveAndPublish: string;
}

/**
 * A component to render a save form, to be used in the integration
 * editor. This does *not* build the form itself, form's field should be passed
 * as the `children` value.
 * @see [i18nTitle]{@link IIntegrationSaveFormProps#i18nTitle}
 */
export const IntegrationSaveForm: React.FunctionComponent<
  IIntegrationSaveFormProps
> = ({
  i18nFormTitle,
  handleSubmit,
  onPublish,
  onSave,
  isSaveDisabled,
  isSaveLoading,
  isPublishDisabled,
  isPublishLoading,
  i18nSave,
  i18nSaveAndPublish,
  children,
}) => {
  return (
    <PageSection>
      <div className="row row-cards-pf">
        <Card
             style={{
               margin: 'auto',
               maxWidth: 600,
             }}
        >
          {i18nFormTitle && (
            <CardTitle>
              <Title className="syn-card__title" headingLevel="h2" size="md">{i18nFormTitle}</Title>
            </CardTitle>
          )}
          <CardBody>
            <Form
              isHorizontal={true}
              onSubmit={handleSubmit}
            >
              {children}
            </Form>
          </CardBody>
          <CardFooter className="syn-card__footer">
            <ButtonLink
              id={'integration-editor-save-button'}
              onClick={onSave}
              disabled={isSaveLoading || isSaveDisabled}
            >
              {isSaveLoading ? <Loader size={'xs'} inline={true} /> : null}
              {i18nSave}
            </ButtonLink>
            &nbsp;
            <ButtonLink
              id={'integration-editor-publish-button'}
              onClick={onPublish}
              as={'primary'}
              disabled={isPublishLoading || isPublishDisabled}
            >
              {i18nSaveAndPublish}
            </ButtonLink>
          </CardFooter>
        </Card>
      </div>
    </PageSection>
  );
};
