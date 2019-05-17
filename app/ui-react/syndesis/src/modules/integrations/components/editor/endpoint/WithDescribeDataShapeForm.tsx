import { DataShapeKinds, toDataShapeKinds } from '@syndesis/api';
import * as H from '@syndesis/history';
import { DataShape } from '@syndesis/models';
import { DescribeDataShapeForm } from '@syndesis/ui';
import * as React from 'react';

export interface IWithDescribeDataShapeFormProps {
  initialKind: string;
  initialDefinition?: string;
  initialName?: string;
  initialDescription?: string;
  backActionHref: H.LocationDescriptor;
  onUpdatedDataShape: (dataShape: DataShape) => void;
}

export interface IWithDescribeDataShapeFormState {
  kind: string;
  definition?: string;
  name?: string;
  description?: string;
}

export class WithDescribeDataShapeForm extends React.Component<
  IWithDescribeDataShapeFormProps,
  IWithDescribeDataShapeFormState
> {
  private definition: string | undefined;
  constructor(props: IWithDescribeDataShapeFormProps) {
    super(props);
    this.state = {
      description: this.props.initialDescription,
      kind: this.props.initialKind,
      name: this.props.initialName,
    };
    this.definition = this.props.initialDefinition;
    this.handleDefinitionChange = this.handleDefinitionChange.bind(this);
    this.handleKindChange = this.handleKindChange.bind(this);
    this.handleNameChange = this.handleNameChange.bind(this);
    this.handleDescriptionChange = this.handleDescriptionChange.bind(this);
    this.handleNext = this.handleNext.bind(this);
  }
  public handleKindChange(kind: string) {
    this.setState({ kind });
  }
  public handleNameChange(name: string) {
    this.setState({ name });
  }
  public handleDescriptionChange(description: string) {
    this.setState({ description });
  }
  public handleDefinitionChange(definition: string) {
    this.definition = definition;
  }
  public handleNext() {
    const metadata =
      toDataShapeKinds(this.state.kind) === DataShapeKinds.ANY
        ? {}
        : { userDefined: 'true' };
    const dataShape =
      toDataShapeKinds(this.state.kind) === DataShapeKinds.ANY
        ? { kind: this.state.kind }
        : {
            description: this.state.description,
            kind: this.state.kind as any,
            metadata,
            name: this.state.name!,
            specification: this.definition,
          };
    this.props.onUpdatedDataShape(dataShape as DataShape);
  }
  public render() {
    return (
      <>
        <DescribeDataShapeForm
          kind={this.state.kind}
          definition={this.definition}
          name={this.state.name}
          description={this.state.description}
          i18nSelectType={'Select Type'}
          i18nSelectTypeHelp={'Indicate how you are specifying the data type.'}
          i18nDataTypeName={'Data Type Name'}
          i18nDataTypeDescription={'Data Type Description'}
          i18nDataTypeDescriptionHelp={
            'Enter any information about this data type that might be helpful to you.'
          }
          i18nDataTypeNameHelp={
            'Enter a name that you choose. This name will appear in the data mapper.'
          }
          i18nDefinition={'Definition'}
          i18nDefinitionHelp={
            'Paste or write content for the type you selected, for example, for JSON Schema, paste the content of a document whose media type is application/schema+json.'
          }
          i18nNext={'Next'}
          i18nBackAction={'Back'}
          backActionHref={this.props.backActionHref}
          onNext={this.handleNext}
          onKindChange={this.handleKindChange}
          onNameChange={this.handleNameChange}
          onDescriptionChange={this.handleDescriptionChange}
          onDefinitionChange={this.handleDefinitionChange}
        />
      </>
    );
  }
}
