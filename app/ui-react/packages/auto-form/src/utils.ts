/**
 * Returns a new configuredProperties object with any default values set from
 * the given definition if they aren't set already
 * @param properties
 * @param initial
 */
import { IFormDefinition, IFormDefinitionProperty } from './models';

export function applyInitialValues<T>(
  properties: IFormDefinition,
  initial?: T
): T {
  const configuredProperties =
    typeof initial !== 'undefined' ? { ...initial } : {};
  Object.keys(properties).forEach(key => {
    const property = properties[key];
    if (
      typeof property.defaultValue !== 'undefined' &&
      typeof configuredProperties[key] === 'undefined'
    ) {
      configuredProperties[key] = property.defaultValue;
    }
  });
  return configuredProperties as T;
}

/**
 * Ensure that the input values match the property definitions
 */
export function sanitizeValues<T>(
  definition: IFormDefinition,
  initialValue: any = {}
): T {
  return Object.keys(definition).reduce((result, key): any => {
    const prop = definition[key];
    if (prop.type === 'legend') {
      return result;
    }
    const value = massageValue(prop, initialValue[key], prop.defaultValue);
    return { ...result, [key]: value };
  }, {}) as T;
}

/**
 * Add the 'name' field from the property ID and sort them by the 'order' property
 */
export function enrichAndOrderProperties(definition: IFormDefinition) {
  return Object.keys(definition)
    .map(key => ({
      ...definition[key],
      name: key,
      required: massageRequired(definition[key]),
      type: massageType(definition[key]),
    }))
    .sort((a, b) => {
      const aOrder = (a.order || 0) as number;
      const bOrder = (b.order || 0) as number;
      return aOrder - bOrder;
    });
}

/**
 * Converts various values passed into the property type to known input types
 *
 * @param property
 */
export function massageType(property: IFormDefinitionProperty) {
  let type = property.type || 'text';
  switch (type) {
    case 'hidden':
      return type;
    case 'int':
    case 'integer':
    case 'long':
      type = 'number';
      break;
    case 'string':
      type = 'text';
      break;
    case 'boolean':
      type = 'checkbox';
  }
  if (typeof property.enum !== 'undefined' && property.enum.length) {
    type = 'select';
  }
  if (typeof property.secret === 'boolean' && property.secret) {
    type = 'password';
  }
  return type;
}

/**
 * Ensure that the 'required' property is false for checkboxes and hidden fields
 *
 * This is a candidate for removal in the future, as it's a workaround
 *
 * @param property
 */
export function massageRequired(property: IFormDefinitionProperty): any {
  switch (property.type) {
    case 'boolean':
    case 'checkbox':
    case 'hidden':
      return false;
    default:
      return property.required;
  }
}

export function getNewArrayRow(definition: IFormDefinition) {
  return sanitizeValues(definition, {});
}

export function getNewArrayRows(missing: number, definition: IFormDefinition) {
  const answer: any[] = [];
  for (let i = 0; i < missing; i++) {
    answer.push(getNewArrayRow(definition));
  }
  return answer;
}

export function sanitizeInitialArrayValue(
  definition: IFormDefinition,
  value?: any[],
  minimum?: number
) {
  const sanitizedValue = (value || []).map(v => sanitizeValues(definition, v));
  const available = sanitizedValue.length;
  const missing = (minimum || 0) - available;
  if (missing < 0) {
    return sanitizedValue;
  }
  return [...sanitizedValue, ...getNewArrayRows(missing, definition)];
}

/**
 * Converts the given value from a string to the type defined in the property definition
 *
 * This is a candidate for removal as it's a workaround
 *
 * @param property
 * @param value
 * @param defaultValue
 */
export function massageValue(
  property: IFormDefinitionProperty,
  value?: any,
  defaultValue?: any
) {
  switch (property.type) {
    case 'number':
      return parseInt(value || defaultValue || 0, 10);
    case 'boolean':
    case 'checkbox':
      return (
        String(value || defaultValue || 'false').toLocaleLowerCase() === 'true'
      );
    case 'array':
      const minElements =
        typeof property.arrayDefinitionOptions !== 'undefined'
          ? property.arrayDefinitionOptions.minElements
          : 0;
      return sanitizeInitialArrayValue(
        property.arrayDefinition || {},
        value || defaultValue,
        minElements
      );
    case 'mapset': {
      const answer: any = value || defaultValue || {};
      return typeof answer === 'string' ? JSON.parse(answer) : answer;
    }
    case 'select': {
      if (property.fieldAttributes && property.fieldAttributes.multiple) {
        return value || defaultValue || [];
      }
      // select controls in syndesis need to default
      // to the first available value if no value
      // is set
      return (
        value ||
        defaultValue ||
        (property.enum && property.enum.length > 0 && property.enum[0].value) ||
        ''
      );
    }
    default: {
      return value || defaultValue || '';
    }
  }
}
