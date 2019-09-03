import {IChoiceConfiguration, IFlowFormOption, IFlowOption} from './interfaces';

/**
 * Builds a sane choice configuration object from the step's configured properties
 * @param configuredProperties
 */
export function createChoiceConfiguration(configuredProperties: {
  [key: string]: any;
}) {
  const flows = (typeof configuredProperties.flows === 'string'
    ? JSON.parse(configuredProperties.flows)
    : configuredProperties.flows || []) as IFlowOption[];
  const defaultFlowEnabled = typeof configuredProperties.default === 'string';
  const defaultFlow = configuredProperties.default;
  const routingScheme = configuredProperties.routingScheme || 'direct';
  return {
    defaultFlow,
    defaultFlowEnabled,
    flows,
    routingScheme,
  } as IChoiceConfiguration;
}

/**
 * Builds a sane flow form option description from either condition expression or path, op, value properties
 * @param option
 */
export function getFlowDescription(option: IFlowFormOption) {
  if (option.condition) {
    return option.condition;
  }

  if (option.path) {
    return '${body.' + option.path + '} ' + option.op + " '" + option.value + "'";
  }

  return "";
}

/**
 * Builds a sane flow option condition expression from either given condition expression or path, op, value properties
 * @param option
 */
export function getConditionExpression(option: IFlowOption) {
  if (option.condition) {
    return option.condition;
  }

  if (option.path) {
    return '${body.' + option.path + '} ' + option.op + " '" + option.value + "'";
  }

  return "";
}
