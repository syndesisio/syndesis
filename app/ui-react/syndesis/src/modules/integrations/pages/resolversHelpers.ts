import { Integration, Step } from '@syndesis/models';
import resolvers from '../resolvers';

export function getCreateAddStepHref(
  integration: Integration,
  position: number
) {
  return resolvers.create.configure.addStep.selectConnection({
    flow: '0',
    integration,
    position: `${position}`,
  });
}

export function getCreateConfigureStepHrefCallback(integration: Integration) {
  return (stepIdx: number, step: Step) => {
    return resolvers.create.configure.editStep.configureAction({
      actionId: step.action!.id!,
      connection: step.connection!,
      flow: '0',
      integration,
      position: `${stepIdx}`,
    });
  };
}

export function getEditAddStepHref(
  integration: Integration,
  flow: string,
  position: number
) {
  return resolvers.integration.edit.addStep.selectConnection({
    flow,
    integration,
    position: `${position}`,
  });
}

export function getEditConfigureStepHrefCallback(
  integration: Integration,
  flow: string
) {
  return (stepIdx: number, step: Step) =>
    resolvers.integration.edit.editStep.configureAction({
      actionId: step.action!.id!,
      connection: step.connection!,
      flow,
      integration,
      position: `${stepIdx}`,
    });
}
