import * as React from 'react';
import { ButtonLink, Container, Loader, PageSection } from '../Layout';

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
}

/**
 * A component to render a save form, to be used in the integration
 * editor. This does *not* build the form itself, form's field should be passed
 * as the `children` value.
 * @see [i18nTitle]{@link IIntegrationSaveFormProps#i18nTitle}
 * @see [i18nSubtitle]{@link IIntegrationSaveFormProps#i18nSubtitle}
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
  children,
}) => {
  return (
    <PageSection>
      <form
        className="form-horizontal required-pf"
        role="form"
        onSubmit={handleSubmit}
        style={{
          margin: 'auto',
          maxWidth: 600,
        }}
      >
        <div className="row row-cards-pf">
          <div className="card-pf">
            {i18nFormTitle && (
              <div className="card-pf-title">{i18nFormTitle}</div>
            )}
            <div className="card-pf-body">
              <Container>{children}</Container>
            </div>
            <div className="card-pf-footer">
              <ButtonLink
                id={'integration-editor-save-button'}
                onClick={onSave}
                disabled={isSaveLoading || isSaveDisabled}
              >
                {isSaveLoading ? <Loader size={'xs'} inline={true} /> : null}
                Save
              </ButtonLink>
              &nbsp;
              <ButtonLink
                id={'integration-editor-publish-button'}
                onClick={onPublish}
                as={'primary'}
                disabled={isPublishLoading || isPublishDisabled}
              >
                Save and publish
              </ButtonLink>
            </div>
          </div>
        </div>
      </form>
    </PageSection>
  );
};
