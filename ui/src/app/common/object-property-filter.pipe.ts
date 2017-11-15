import { Pipe, PipeTransform } from '@angular/core';

export class ObjectPropertyFilterConfig {
  filter: any;
  propertyName: string;
  exact?: boolean;
}

export function getPropertyValue(obj: any, prop: string): any {
  const parts = prop.split('.');
  const part = parts.shift();
  const value = obj[part];
  if (parts.length) {
    return getPropertyValue(value, parts.join('.'));
  } else {
    return value;
  }
}

@Pipe({
  name: 'objectPropertyFilter',
  pure: false
})
export class ObjectPropertyFilterPipe implements PipeTransform {
  transform(objects: any[], config: ObjectPropertyFilterConfig) {
    if (!config || !('filter' in config) || !('propertyName' in config)) {
      return objects;
    }
    return objects.filter((obj: any) => {
      const value = getPropertyValue(obj, config.propertyName);
      if (value === undefined) {
        // no idea what to do in this case
        return true;
      }
      switch (typeof config.filter) {
        case 'string':
          if (config.exact && config.filter.length > 0) {
            return <string>value === config.filter;
          }
          return (
            (<string>value)
              .toLowerCase()
              .indexOf(config.filter.toLowerCase()) !== -1
          );
        case 'function':
          return config.filter(value);
        case 'number':
        case 'boolean':
          return value === config.filter;
        case 'object':
          return JSON.stringify(value) === JSON.stringify(config.filter);
        default:
          return false;
      }
    });
  }
}
