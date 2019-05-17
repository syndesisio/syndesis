import { IFormDefinition, IFormDefinitionProperty } from '@syndesis/auto-form';
import {
  IConfigurationProperties,
  IConfigurationProperty,
  StringMap,
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
  const {
    cols,
    max,
    min,
    multiple,
    rows,
    controlHint,
    controlTooltip,
    labelHint,
    labelTooltip,
    ...formDefinitionProperty
  } = property as any; // needed, ConfigurationProperty is a lie
  return {
    ...formDefinitionProperty,
    controlHint: controlHint || controlTooltip,
    fieldAttributes: {
      cols,
      max,
      min,
      multiple,
      rows,
    },
    labelHint: labelHint || labelTooltip,
  } as IFormDefinitionProperty;
}

/**
 * Returns a new configuredProperties object with any default values set from
 * the given definition if they aren't set already
 * @param properties
 * @param initial
 */
export function applyInitialValues(
  properties: IConfigurationProperties,
  initial?: StringMap<string>
) {
  const configuredProperties =
    typeof initial !== 'undefined' ? { ...initial } : {};
  Object.keys(properties).forEach(key => {
    const property = properties[key];
    // `property.value` being set is deprecated, defaultValue takes precedence
    if (
      typeof property.value !== 'undefined' &&
      typeof configuredProperties[key] === 'undefined'
    ) {
      configuredProperties[key] = property.value;
    }
    if (
      typeof property.defaultValue !== 'undefined' &&
      typeof configuredProperties[key] === 'undefined'
    ) {
      configuredProperties[key] = property.defaultValue;
    }
  });
  return configuredProperties;
}

export function validateConfiguredProperties(
  properties: IConfigurationProperties,
  values?: { [name: string]: any }
) {
  if (typeof values === 'undefined') {
    return false;
  }
  const allRequired = Object.keys(properties).filter(
    key => properties[key].required
  );
  if (allRequired.length === 0) {
    return true;
  }
  const allRequiredSet = allRequired
    .map(key => typeof values[key] !== 'undefined')
    .reduce((prev, curr) => curr, false);
  return allRequiredSet;
}
