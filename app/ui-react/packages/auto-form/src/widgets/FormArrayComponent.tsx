import * as React from 'react';
import { FormBuilder } from '../FormBuilder';
import { IFormArrayControlProps, IFormArrayDefinitionOptions } from '../models';
import { getArrayRows } from '../utils';
import { toValidHtmlId } from './helpers';
import { TextButton } from './TextButton';

import './FormArrayComponent.css';

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
    const definition = this.props.property.arrayDefinition;
    const options =
      this.props.property.arrayDefinitionOptions ||
      ({} as IFormArrayDefinitionOptions);
    const formGroupAttributes = options.formGroupAttributes || {};
    const fieldAttributes = options.fieldAttributes || {};
    const controlLabelAttributes = options.controlLabelAttributes || {};
    const arrayControlAttributes = options.arrayControlAttributes || {};
    const arrayRowTitleAttributes = options.arrayRowTitleAttributes || {};
    const minElements = options.minElements || 0;
    const values = this.props.form.values[this.props.name] || [];
    return (
      <>
        {values.map((value: any, index: number) => {
          const fieldName = `${this.props.name}[${index}]`;
          return (
            <FormBuilder
              key={fieldName}
              definition={definition}
              initialValue={value}
              customComponents={this.props.customComponents}
              i18nRequiredProperty={''}
            >
              {({ initialValue: rowValue, propertiesArray, getField }) => {
                const titleKey = `${fieldName}-title`;
                const controlGroupName = `${fieldName}-array-controls`;
                return (
                  <fieldset>
                    {options.rowTitle && (
                      <div key={titleKey} {...arrayRowTitleAttributes}>
                        <h5 className="form-array-control__row-title">
                          <strong>{`${index + 1}. ${options.rowTitle}`}</strong>
                        </h5>
                      </div>
                    )}
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
                      <div id={toValidHtmlId(`${controlGroupName}-control`)}>
                        <div className="form-array-control__array-controls">
                          {options.showSortControls && (
                            <>
                              <TextButton
                                onClick={() => {
                                  this.props.move(index, index - 1);
                                }}
                                visible={index > 0}
                              >
                                <i className="fa fa-arrow-circle-o-up" />
                              </TextButton>
                              <TextButton
                                onClick={() => {
                                  this.props.move(index, index + 1);
                                }}
                                visible={index < values.length - 1}
                              >
                                <i className="fa fa-arrow-circle-o-down" />
                              </TextButton>
                            </>
                          )}
                          <TextButton
                            onClick={() => this.props.remove(index)}
                            visible={index + 1 > minElements}
                          >
                            <i className="fa fa-trash-o" />
                          </TextButton>
                        </div>
                        <div className="help-block">&nbsp;</div>
                      </div>
                    </div>
                  </fieldset>
                );
              }}
            </FormBuilder>
          );
        })}
        <TextButton
          onClick={() => this.props.push(getArrayRows(1, definition))}
        >
          {options.i18nAddElementText || '+ Add Another'}
        </TextButton>
      </>
    );
  }
}
