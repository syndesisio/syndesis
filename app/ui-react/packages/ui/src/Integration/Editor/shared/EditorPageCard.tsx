import * as React from 'react';
import { ButtonLink, Container, PageSection } from '../../../Layout';

export interface IEditorPageCardProps {
  header?: JSX.Element;
  i18nDone: string;
  isValid: boolean;
  submitForm: (e?: any) => void;
}

export class EditorPageCard extends React.Component<IEditorPageCardProps> {
  public render() {
    return (
      <PageSection>
        <Container>
          <div className="row row-cards-pf">
            <div className="card-pf">
              {this.props.header && (
                <div className="card-pf-header">{this.props.header}</div>
              )}
              <div className="card-pf-body">
                <Container>{this.props.children}</Container>
              </div>
              <div className="card-pf-footer">
                <ButtonLink
                  data-testid={'editor-page-card-done-button'}
                  onClick={this.props.submitForm}
                  disabled={!this.props.isValid}
                  as={'primary'}
                >
                  {this.props.i18nDone}
                </ButtonLink>
              </div>
            </div>
          </div>
        </Container>
      </PageSection>
    );
  }
}
