import * as React from 'react';
import { Container } from '../Layout';

export interface IIntegrationEditorFormProps {
  /**
   * The internationalized title.
   */
  i18nTitle: string;
  /**
   * The internationalized subtitle.
   */
  i18nSubtitle: string;
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
 * @see [i18nTitle]{@link IIntegrationEditorFormProps#i18nTitle}
 * @see [i18nSubtitle]{@link IIntegrationEditorFormProps#i18nSubtitle}
 */
export class IntegrationEditorForm extends React.Component<
  IIntegrationEditorFormProps
> {
  public render() {
    return (
      <Container>
        <h1>{this.props.i18nTitle} - Choose Action</h1>
        <p>{this.props.i18nSubtitle}</p>
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
      </Container>
    );
  }
}
