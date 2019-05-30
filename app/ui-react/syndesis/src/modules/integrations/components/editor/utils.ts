import {
  ADVANCED_FILTER,
  AGGREGATE,
  API_PROVIDER,
  BASIC_FILTER,
  CONNECTOR,
  DATA_MAPPER,
  DataShapeKinds,
  ENDPOINT,
  EXTENSION,
  getExtensionIcon,
  getNextAggregateStep,
  getPreviousStepWithDataShape,
  getStepIcon,
  HIDE_FROM_STEP_SELECT,
  LOG,
  SPLIT,
  TEMPLATE,
  toDataShapeKinds,
} from '@syndesis/api';
import * as H from '@syndesis/history';
import {
  Connection,
  ConnectionOverview,
  ConnectorAction,
  DataShape,
  Extension,
  Step,
  StepKind,
} from '@syndesis/models';
import {
  ISelectConnectionRouteParams,
  ISelectConnectionRouteState,
  IUIIntegrationStep,
  IUIStep,
} from './interfaces';

type StepKindHrefCallback = (
  step: StepKind,
  p: ISelectConnectionRouteParams,
  s: ISelectConnectionRouteState
) => H.LocationDescriptorObject;

export function getStepKind(step: Step): IUIStep['uiStepKind'] {
  if (
    step.connection &&
    step.connection.id === API_PROVIDER &&
    !(step.metadata || {}).configured
  ) {
    return API_PROVIDER;
  }
  return step.stepKind;
}

export function toUIStep(step: Step): IUIStep {
  const uiStepKind = getStepKind(step);
  const inputDataShape =
    step.action &&
    step.action.descriptor &&
    step.action.descriptor.inputDataShape;
  const outputDataShape =
    step.action &&
    step.action.descriptor &&
    step.action.descriptor.outputDataShape;
  switch (uiStepKind) {
    case EXTENSION:
      // An extension needs special mapping
      return {
        ...step,
        description:
          (step as StepKind).description ||
          (step as StepKind).extension!.description ||
          '',
        icon: getStepIcon(process.env.PUBLIC_URL, step),
        inputDataShape,
        metadata: {
          ...(step.extension!.metadata || {}),
          ...(step.metadata || {}),
        },
        name: step.name || step.extension!.name,
        outputDataShape,
        properties: step.action!.descriptor!.propertyDefinitionSteps![0]
          .properties,
        title: step.name!,
        uiStepKind,
      };
    case API_PROVIDER:
    case ENDPOINT:
    case CONNECTOR:
      // this step is a Connection step
      return {
        ...step,
        description:
          (step as ConnectionOverview).description ||
          step.connection!.description ||
          '',
        icon: getStepIcon(process.env.PUBLIC_URL, step),
        inputDataShape,
        metadata: {
          ...(step.connection!.metadata || {}),
          ...(step.metadata || {}),
        },
        name: step.name || step.connection!.connector!.name!,
        outputDataShape,
        properties:
          step.action &&
          step.action.descriptor &&
          step.action.descriptor.propertyDefinitionSteps &&
          step.action.descriptor.propertyDefinitionSteps.length &&
          step.action.descriptor.propertyDefinitionSteps[0].properties,
        title:
          `${(step.connection && step.connection.name) || '<unknown>'}` +
          step.action
            ? ` - ${(step.action && step.action.name) || '<unknown>'}`
            : '',
        uiStepKind,
      };
    default:
      // this is a regular built-in step
      const name = step.name || step.stepKind || 'Step';
      return {
        ...(step as StepKind),
        icon: getStepIcon(process.env.PUBLIC_URL, step),
        inputDataShape,
        metadata: step.metadata || {},
        name,
        outputDataShape,
        title: name,
        uiStepKind,
      };
  }
}

export function toUIStepCollection(steps: Step[]): IUIStep[] {
  return steps.map(toUIStep);
}

export function isSameDataShape(one: DataShape, other: DataShape): boolean {
  if (!one || !other) {
    return false;
  }
  return (
    one.kind === other.kind &&
    one.type === other.type &&
    one.specification === other.specification
  );
}

export function toUIIntegrationStepCollection(
  steps: IUIStep[]
): IUIIntegrationStep[] {
  return steps.map((step, position) => {
    let previousStepShouldDefineDataShape = false;
    let shouldAddDataMapper = false;
    const isUnclosedSplit =
      step.stepKind === SPLIT &&
      getNextAggregateStep(steps, position) === undefined;

    const shape =
      position === 0
        ? getDataShapeText(step.stepKind!, step.outputDataShape)
        : getDataShapeText(step.stepKind!, step.inputDataShape);

    if (step.action && step.action.descriptor && position > 0) {
      const inputDataShape = step.action.descriptor.inputDataShape;
      if (
        inputDataShape &&
        ![DataShapeKinds.ANY, DataShapeKinds.NONE].includes(
          toDataShapeKinds(inputDataShape.kind!)
        )
      ) {
        const prev = getPreviousStepWithDataShape(steps, position);
        if (
          prev &&
          prev.action &&
          prev.action.descriptor &&
          prev.action.descriptor.outputDataShape
        ) {
          const prevOutDataShape = prev.action.descriptor.outputDataShape;
          if (DataShapeKinds.ANY === toDataShapeKinds(prevOutDataShape.kind!)) {
            previousStepShouldDefineDataShape = true;
          } else if (!isSameDataShape(inputDataShape, prevOutDataShape)) {
            shouldAddDataMapper = true;
          }
        }
      }
    }

    return {
      ...step,
      isUnclosedSplit,
      previousStepShouldDefineDataShape,
      shape,
      shouldAddDataMapper,
    };
  });
}

export function getDataShapeText(stepKind: string, dataShape?: DataShape) {
  if (!dataShape) {
    return undefined;
  }
  const isCollection =
    dataShape.metadata && dataShape.metadata.variant === 'collection';
  let answer: string | undefined = dataShape.name;
  if (dataShape.kind) {
    if (toDataShapeKinds(dataShape.kind) === DataShapeKinds.ANY) {
      answer = 'ANY';
    } else if (toDataShapeKinds(dataShape.kind) === DataShapeKinds.NONE) {
      answer = undefined;
    } else if (!dataShape.type) {
      answer = dataShape.kind;
    }
  }
  // TODO "split" currently appears to have variant set but maybe it shouldn't
  if (answer && isCollection && stepKind !== SPLIT) {
    answer = answer + ' (Collection)';
  }
  return answer;
}

export interface IGetStepHrefs {
  apiProviderHref: StepKindHrefCallback;
  connectionHref: StepKindHrefCallback;
  filterHref: StepKindHrefCallback;
  mapperHref: StepKindHrefCallback;
  templateHref: StepKindHrefCallback;
  stepHref: StepKindHrefCallback;
}
export const getStepHref = (
  step: Step,
  params: ISelectConnectionRouteParams,
  state: ISelectConnectionRouteState,
  hrefs: IGetStepHrefs
) => {
  switch (getStepKind(step)) {
    case API_PROVIDER:
      return hrefs.apiProviderHref(step as StepKind, params, state);
    case ENDPOINT:
    case CONNECTOR:
      return hrefs.connectionHref(
        typeof (step as IUIStep).uiStepKind !== 'undefined'
          ? ((step as IUIStep).connection! as StepKind)
          : (step as StepKind),
        params,
        state
      );
    case BASIC_FILTER:
      return hrefs.filterHref(step as StepKind, params, state);
    case DATA_MAPPER:
      return hrefs.mapperHref(step as StepKind, params, state);
    case TEMPLATE:
      return hrefs.templateHref(step as StepKind, params, state);
    case EXTENSION:
    default:
      return hrefs.stepHref(step as StepKind, params, state);
  }
};

export function mergeConnectionsSources(
  connections: ConnectionOverview[],
  extensions: Extension[],
  steps: StepKind[]
): IUIStep[] {
  return [
    ...connections.map(connection =>
      toUIStep({
        connection,
        name: connection.name,
        stepKind: ENDPOINT,
      } as StepKind)
    ),
    ...extensions.reduce(
      (extentionsByAction, extension) => {
        extension.actions.forEach(a => {
          let properties = {};
          if (
            a.descriptor &&
            Array.isArray(a.descriptor.propertyDefinitionSteps)
          ) {
            properties = a.descriptor.propertyDefinitionSteps.reduce(
              (acc, current) => {
                return { ...acc, ...current.properties };
              },
              {}
            );
          }
          if (a.actionType === 'step') {
            extentionsByAction.push(
              toUIStep({
                action: a,
                configuredProperties: undefined,
                description: a.description || '',
                extension,
                icon: `${process.env.PUBLIC_URL}${getExtensionIcon(extension)}`,
                metadata: (extension.metadata as { [name: string]: any }) || {},
                name: a.name,
                properties,
                stepKind: EXTENSION,
                title: a.name,
              } as StepKind)
            );
          }
        });
        return extentionsByAction;
      },
      [] as IUIStep[]
    ),
    ...steps.map(s => toUIStep(s)),
  ]
    .filter(s => !!s.uiStepKind) // this should never happen
    .sort((a, b) => a.name.localeCompare(b.name));
}

/**
 * Filters connections based on the supplied position in the step array
 * @param steps
 * @param position
 * @param atStart
 * @param atEnd
 */
export function filterStepsByPosition(
  steps: StepKind[],
  position: number,
  atStart: boolean,
  atEnd: boolean
) {
  if (typeof position === 'undefined' || !steps) {
    // safety net
    return steps;
  }
  return steps.filter(step => {
    // Hide steps that are marked as such, and specifically the log connection
    if (
      (typeof step.connection !== 'undefined' &&
        typeof step.connection.metadata !== 'undefined' &&
        step.connection.metadata[HIDE_FROM_STEP_SELECT]) ||
      (typeof step.metadata !== 'undefined' &&
        step.metadata[HIDE_FROM_STEP_SELECT]) ||
      (step.connection || (step as Connection)).connectorId === LOG
    ) {
      return false;
    }
    // Special handling for the beginning of a flow
    if (atStart) {
      // At the moment only endpoints can be at the start
      if (step.stepKind !== ENDPOINT) {
        return false;
      }
      if ((step.connection || (step as Connection)).connector) {
        return (
          step.connection || (step as Connection)
        ).connector!.actions.some((action: ConnectorAction) => {
          return action.pattern === 'From';
        });
      }
      // it's not a connection
      return true;
    }
    // Special handling for the end of a flow
    if (atEnd) {
      // Several step kinds aren't usable at the end of a flow
      switch ((step as Step).stepKind) {
        case DATA_MAPPER:
        case BASIC_FILTER:
        case ADVANCED_FILTER:
        case SPLIT:
        case AGGREGATE:
        case TEMPLATE:
          return false;
        default:
      }
    }
    if (
      (step.connection || (step as Connection)).connectorId === API_PROVIDER
    ) {
      // api provider can be used only for From actions
      return false;
    }
    // All non-connection steps can be shown, except the above
    if (step.stepKind !== ENDPOINT) {
      return true;
    }
    // Only show connections that have at least one action that accepts data
    if ((step.connection || (step as Connection)).connector) {
      return (step.connection || (step as Connection)).connector!.actions.some(
        (action: ConnectorAction) => {
          return action.pattern === 'To';
        }
      );
    }
    return true;
  });
}

/**
 * Filters connections based on the supplied position in the step array and their
 * visibility status
 * @param steps
 * @param position
 * @param flowSteps
 */
export function visibleStepsByPosition(
  steps: StepKind[],
  position: number,
  flowSteps: Step[]
) {
  const previousSteps = flowSteps.slice(0, position);
  const subsequentSteps = flowSteps.slice(position);
  return filterStepsByPosition(
    steps,
    position,
    previousSteps.length === 0,
    subsequentSteps.length === 0
  ).filter(s => {
    if (typeof s.visible === 'function') {
      return s.visible(position, previousSteps, subsequentSteps);
    }
    return true;
  });
}
