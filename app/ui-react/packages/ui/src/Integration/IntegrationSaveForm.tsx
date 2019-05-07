import * as React from 'react';
import { Container, PageSection } from '../Layout';

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
}

/**
 * A component to render a save form, to be used in the integration
 * editor. This does *not* build the form itself, form's field should be passed
 * as the `children` value.
 * @see [i18nTitle]{@link IIntegrationSaveFormProps#i18nTitle}
 * @see [i18nSubtitle]{@link IIntegrationSaveFormProps#i18nSubtitle}
 */
export class IntegrationSaveForm extends React.Component<
  IIntegrationSaveFormProps
> {
  public render() {
    return (
      <PageSection>
        <form
          className="form-horizontal required-pf"
          role="form"
          onSubmit={this.props.handleSubmit}
        >
          <div className="row row-cards-pf">
            <div className="card-pf">
              {this.props.i18nFormTitle && (
                <div className="card-pf-title">{this.props.i18nFormTitle}</div>
              )}
              <div className="card-pf-body">
                <Container>{this.props.children}</Container>
              </div>
            </div>
          </div>
        </form>
      </PageSection>
    );
  }
}
