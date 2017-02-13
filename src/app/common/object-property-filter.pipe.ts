import { Pipe } from '@angular/core';

export class ObjectPropertyFilterConfig {
  filter: any;
  propertyName: string;
}


@Pipe({
  name: 'objectPropertyFilter',
  pure: false,
})
export class ObjectPropertyFilterPipe {
  transform(objects:any[], config:ObjectPropertyFilterConfig) {
    if (!config || !('filter' in config) || !('propertyName' in config)) {
      return objects;
    }
    function getPropertyValue(obj:any, prop:string):any {
      let parts = prop.split('.');
      let part = parts.shift();
      let value = obj[part];
      if (parts.length) {
        return getPropertyValue(value, parts.join('.'));
      } else {
        return value;
      }
    }
    return objects.filter((obj:any) => {
      let value = getPropertyValue(obj, config.propertyName);
      switch (typeof config.filter) {
        case 'string':
          return (<string>value).toLowerCase().indexOf(config.filter.toLowerCase()) !== -1;
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
