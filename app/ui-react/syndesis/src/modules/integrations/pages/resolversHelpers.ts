import { Integration, Step } from '@syndesis/models';
import resolvers from '../resolvers';

export function getCreateAddStepHref(
  integration: Integration,
  position: number
) {
  return resolvers.create.configure.addStep.selectConnection({
    integration,
    position: `${position}`,
  });
}

export function getCreateConfigureStepHrefCallback(integration: Integration) {
  return (stepIdx: number, step: Step) =>
    resolvers.create.configure.editStep.configureAction({
      actionId: step.action!.id!,
      integration,
      position: `${stepIdx}`,
    });
}

export function getEditAddStepHref(integration: Integration, position: number) {
  return resolvers.integration.edit.addStep.selectConnection({
    integration,
    position: `${position}`,
  });
}

export function getEditConfigureStepHrefCallback(integration: Integration) {
  return (stepIdx: number, step: Step) =>
    resolvers.integration.edit.editStep.configureAction({
      actionId: step.action!.id!,
      integration,
      position: `${stepIdx}`,
    });
}
