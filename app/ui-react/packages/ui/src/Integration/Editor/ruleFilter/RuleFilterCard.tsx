import * as React from 'react';
import { ButtonLink, Container, PageSection } from '../../../Layout';

export interface IRuleFilterCardProps {
  i18nDone: string;
  isValid: boolean;
  submitForm: (e?: any) => void;
}

export class RuleFilterCard extends React.Component<IRuleFilterCardProps> {
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
