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
 * Builds a sane flow form option description from either condition expression directly or from path, op, value properties
 * @param option
 */
export function getFlowDescription(option: IFlowFormOption) {
  return buildConditionExpression(option.condition, option.path, option.op, option.value);
}

/**
 * Builds a sane flow option description from either condition expression directly or from path, op, value properties
 * @param option
 */
export function getConditionExpression(option: IFlowOption) {
  return buildConditionExpression(option.condition, option.path, option.op, option.value);
}

/**
 * Builds a sane flow form option description from either condition expression or path, op, value properties
 * @param condition
 * @param path
 * @param op
 * @param value
 */
function buildConditionExpression(condition: string | undefined, path: string | undefined, op: string | undefined, value: string | undefined) {
  if (condition) {
    return condition;
  }

  if (path) {
    return `\${body.${path}} ${op} '${value}'`;
  }

  return "";
}
