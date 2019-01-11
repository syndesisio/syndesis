import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { Breadcrumb, PageHeader } from '@syndesis/ui';
import { IntegrationActionConfigurationForm } from '@syndesis/ui';
import * as H from 'history';
import * as React from 'react';

export interface IIntegrationEditorConfigureConnection {
  breadcrumb: JSX.Element[];
  disabled?: boolean;
  definition: IFormDefinition;
  i18nTitle: string;
  i18nSubtitle: string;
  moreSteps: boolean;
  backLink: H.LocationDescriptor;
  onSave(configuredProperties: { [key: string]: string }): void;
}

export class IntegrationEditorConfigureConnection extends React.Component<
  IIntegrationEditorConfigureConnection
> {
  public render() {
    return (
      <>
        <PageHeader>
          <Breadcrumb>{this.props.breadcrumb}</Breadcrumb>

          <h1>{this.props.i18nTitle}</h1>
          <p>{this.props.i18nSubtitle}</p>
        </PageHeader>
        <AutoForm<{ [key: string]: string }>
          i18nRequiredProperty={'* Required field'}
          definition={this.props.definition}
          initialValue={{}}
          onSave={this.props.onSave}
        >
          {({ fields, handleSubmit }) => (
            <IntegrationActionConfigurationForm
              backLink={this.props.backLink}
              fields={fields}
              handleSubmit={handleSubmit}
              i18nBackLabel={'< Choose action'}
              i18nSubmitLabel={this.props.moreSteps ? 'Continue' : 'Done'}
            />
          )}
        </AutoForm>
      </>
    );
  }
}
