import * as React from 'react';
import { FormBuilder } from '../FormBuilder';
import { IFormArrayControlProps, IFormArrayDefinitionOptions } from '../models';
import { toValidHtmlId } from './helpers';
import { TextButton } from './TextButton';

export interface IFormArrayComponentProps extends IFormArrayControlProps {
  customComponents: { [key: string]: any };
}

export class FormArrayComponent extends React.Component<
  IFormArrayComponentProps | any /* todo type coercion */
> {
  public render() {
    if (typeof this.props.property.arrayDefinition === 'undefined') {
      return (
        <div className="alert alert-warning">
          <span>No Array definition supplied for array type</span>
        </div>
      );
    }
    const options =
      this.props.property.arrayDefinitionOptions ||
      ({} as IFormArrayDefinitionOptions);
    const formGroupAttributes = options.formGroupAttributes || {};
    const fieldAttributes = options.fieldAttributes || {};
    const controlLabelAttributes = options.controlLabelAttributes || {};
    const arrayControlAttributes = options.arrayControlAttributes || {};
    const minElements = options.minElements || 0;
    const values = this.props.form.values[this.props.name] || [];
    return (
      <>
        {values.map((value: any, index: number) => {
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
                const controlGroupName = `${fieldName}-formArrayControls`;
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
                      key={controlGroupName}
                      className={'form-group'}
                      {...formGroupAttributes}
                      {...arrayControlAttributes}
                    >
                      <label
                        htmlFor={toValidHtmlId(`${controlGroupName}-control`)}
                        className="control-label"
                        {...controlLabelAttributes}
                      >
                        &nbsp;
                      </label>
                      <div
                        id={toValidHtmlId(`${controlGroupName}-control`)}
                        className=""
                      >
                        <div className="pull-right">
                          {index + 1 > minElements ? (
                            <TextButton
                              onClick={() => this.props.remove(index)}
                            >
                              <i
                                className="fa fa-trash-o"
                                style={{ fontSize: '20px' }}
                              />
                            </TextButton>
                          ) : (
                            <>&nbsp;</>
                          )}
                        </div>
                      </div>
                      <div className="help-block">&nbsp;</div>
                    </div>
                  </fieldset>
                );
              }}
            </FormBuilder>
          );
        })}
        <TextButton onClick={() => this.props.push({})}>
          {options.i18nAddElementText || '+ Add Another'}
        </TextButton>
      </>
    );
  }
}
