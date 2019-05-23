import * as React from 'react';
import { FormBuilder } from '../FormBuilder';
import { IFormArrayControlProps, IFormArrayDefinitionOptions } from '../models';
import { TextButton } from './TextButton';

export interface IFormArrayComponentProps extends IFormArrayControlProps {
  customComponents: { [key: string]: any };
}

export class FormArrayComponent extends React.Component<
  IFormArrayComponentProps | any /* todo type coercion */
> {
  public render() {
    const options =
      this.props.property.arrayDefinitionOptions ||
      ({} as IFormArrayDefinitionOptions);
    const formGroupAttributes = options.formGroupAttributes || {};
    const fieldAttributes = options.fieldAttributes || {};
    const controlLabelAttributes = options.controlLabelAttributes || {};
    const arrayControlAttributes = options.arrayControlAttributes || {};
    return (
      <>
        {(this.props.form.values[this.props.name] || [{}]).map(
          (value: any, index: number) => {
            const fieldName = `${this.props.name}[${index}]`;
            return (
              <FormBuilder
                key={fieldName}
                definition={this.props.property.arrayDefinition}
                initialValue={value}
                customComponents={this.props.customComponents}
                i18nRequiredProperty={''}
              >
                {({ initialValue: rowValue, propertiesArray, getField }) => {
                  return (
                    <fieldset>
                      {propertiesArray.map(property =>
                        getField({
                          allFieldsRequired:
                            this.props.allFieldsRequired || false,
                          key: `${fieldName}.${property.name}`,
                          name: `${fieldName}.${property.name}`,
                          property: {
                            controlLabelAttributes,
                            fieldAttributes,
                            formGroupAttributes,
                            ...property,
                          },
                          value: rowValue[property.name],
                        })
                      )}
                      <div
                        key={`${fieldName}.formArrayControls`}
                        className={'form-group'}
                        {...formGroupAttributes}
                        {...arrayControlAttributes}
                      >
                        {index > 0 ? (
                          <div className="pull-right">
                            <TextButton
                              onClick={() => this.props.remove(index)}
                            >
                              <i
                                className="fa fa-trash-o"
                                style={{ fontSize: '20px' }}
                              />
                            </TextButton>
                          </div>
                        ) : (
                          <>&nbsp;</>
                        )}
                      </div>
                    </fieldset>
                  );
                }}
              </FormBuilder>
            );
          }
        )}
        <TextButton onClick={() => this.props.push({})}>
          {this.props.property.arrayDefinitionOptions.i18nAddElementText}
        </TextButton>
      </>
    );
  }
}
