import { Form } from '@patternfly/react-core';
import { Alert } from 'patternfly-react';
import * as React from 'react';
import { Container } from '../../../Layout';

export interface IViewConfigurationFormValidationResult {
  message: string;
  type: 'error' | 'success';
}

export interface IViewConfigurationFormProps {
  /**
   * The internationalized form title.
   */
  i18nFormTitle?: string;
  /**
   * Form level validationResults
   */
  validationResults?: IViewConfigurationFormValidationResult[];
  /**
   * The callback fired when submitting the form.
   * @param e
   */
  handleSubmit: (e?: any) => void;
}

/**
 * A component to render a save form, to be used in the create view wizard.
 * This does *not* build the form itself, form's field should be passed
 * as the `children` value.
 * @see [i18nTitle]{@link IViewConfigurationFormProps#i18nTitle}
 * @see [i18nSubtitle]{@link IViewConfigurationFormProps#i18nSubtitle}
 */
export class ViewConfigurationForm extends React.Component<
  IViewConfigurationFormProps
> {
  public static defaultProps = {
    validationResults: [],
  };

  public render() {
    return (
      <Container>
        <div className="row row-cards-pf">
          <div className="card-pf">
            {this.props.i18nFormTitle && (
              <div className="card-pf-title">{this.props.i18nFormTitle}</div>
            )}
            <div className="card-pf-body">
              {this.props.validationResults!.map((e, idx) => (
                <Alert key={idx} type={e.type}>
                  {e.message}
                </Alert>
              ))}
              <Container>
                <Form isHorizontal={true} onSubmit={this.props.handleSubmit}>
                  {this.props.children}
                </Form>
              </Container>
            </div>
          </div>
        </div>
      </Container>
    );
  }
}
