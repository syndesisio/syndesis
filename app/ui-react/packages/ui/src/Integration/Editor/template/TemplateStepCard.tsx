import * as React from 'react';
import { ButtonLink, Container, PageSection } from '../../../Layout';

export interface ITemplateStepCardProps {
  i18nDone: string;
  isValid: boolean;
  submitForm: (e?: any) => void;
}

export class TemplateStepCard extends React.Component<ITemplateStepCardProps> {
  public render() {
    return (
      <PageSection>
        <Container>
          <div className="row row-cards-pf">
            <div className="card-pf">
              <div className="card-pf-body">
                <Container>{this.props.children}</Container>
              </div>
              <div className="card-pf-footer">
                <ButtonLink
                  data-testid={'template-step-card-done-button'}
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
