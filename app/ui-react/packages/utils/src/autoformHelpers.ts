import { IFormDefinition, IFormDefinitionProperty } from '@syndesis/auto-form';
import {
  IConfigurationProperties,
  IConfigurationProperty,
} from '@syndesis/models';

/**
 * Maps an API map of ConfigurationProperty objects to
 * an autoform IFormDefinition object.  Use on properties
 * objects from backend responses to ensure they're mapped
 * properly
 *
 * @param properties
 */
export function toFormDefinition(properties: IConfigurationProperties) {
  if (!properties) {
    throw new Error('Undefined value passed to form definition converter');
  }
  const answer: IFormDefinition = {};
  Object.keys(properties).forEach(key => {
    answer[key] = toFormDefinitionProperty(properties[key]);
  });
  return answer;
}

/**
 * Maps an API ConfigurationProperty object to an autoform IFormDefinitionPropertyObject
 * @param property
 */
export function toFormDefinitionProperty(property: IConfigurationProperty) {
  // yes, this type coercion is needed because property
  const {
    cols,
    max,
    min,
    multiple,
    rows,
    ...formDefinitionProperty
  } = property;
  return {
    ...formDefinitionProperty,
    fieldAttributes: {
      cols,
      max,
      min,
      multiple,
      rows,
    },
  } as IFormDefinitionProperty;
}
