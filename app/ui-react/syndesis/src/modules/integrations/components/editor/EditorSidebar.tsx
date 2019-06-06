import { Step } from '@syndesis/models';
import {
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
} from '@syndesis/ui';
import * as React from 'react';
import { EntityIcon } from '../../../../shared';
import { IUIStep } from './interfaces';
import { getDataShapeText, toUIStepCollection } from './utils';

function makeActiveStep(
  position: number,
  expanded: boolean,
  title: string,
  tooltip: string,
  activeStep?: IUIStep
) {
  return activeStep ? (
    <IntegrationFlowStepWithOverview
      icon={
        <EntityIcon
          alt={activeStep.name}
          entity={activeStep}
          width={24}
          height={24}
        />
      }
      i18nTitle={`${position}. ${activeStep.name}`}
      i18nTooltip={`${position}. ${activeStep.title}`}
      active={true}
      showDetails={expanded}
      name={
        activeStep.action ? activeStep.action.name : 'Select/configure action'
      }
      action={activeStep.action && activeStep.action.name}
      dataType={getDataShapeText(
        activeStep.stepKind!,
        activeStep.outputDataShape
      )}
    />
  ) : (
    <IntegrationFlowStepGeneric
      icon={<i className={'fa fa-plus'} />}
      i18nTitle={`${position}. ${title}`}
      i18nTooltip={tooltip}
      active={true}
      showDetails={expanded}
      description={tooltip}
    />
  );
}

export interface IEditorSidebarProps {
  steps: Step[];
  activeStep?: IUIStep;
  activeIndex: number;
  initialExpanded?: boolean;
}
export const EditorSidebar: React.FunctionComponent<
  IEditorSidebarProps & {
    isAdding: boolean;
  }
> = ({ activeIndex, activeStep, initialExpanded, steps, isAdding }) => {
  const UISteps = toUIStepCollection(steps);
  return (
    <IntegrationVerticalFlow initialExpanded={initialExpanded}>
      {({ expanded }) => {
        if (UISteps.length === 0) {
          return (
            <>
              {makeActiveStep(
                1,
                expanded,
                'Start',
                'Choose a step',
                activeStep
              )}
              <IntegrationFlowStepGeneric
                icon={<i className={'fa fa-plus'} />}
                i18nTitle={'2. Finish'}
                i18nTooltip={'Finish'}
                active={false}
                showDetails={expanded}
                description={'Choose a connection'}
              />
            </>
          );
        } else if (UISteps.length === 1) {
          const startStep = UISteps[0];
          return (
            <>
              <IntegrationFlowStepWithOverview
                icon={
                  <EntityIcon
                    alt={'Step'}
                    entity={startStep}
                    width={24}
                    height={24}
                  />
                }
                i18nTitle={`1. ${startStep.name}`}
                i18nTooltip={`1. ${startStep.title}`}
                showDetails={expanded}
                name={startStep.name}
                action={startStep.action && startStep.action.name!}
                dataType={getDataShapeText(
                  startStep.stepKind!,
                  startStep.outputDataShape
                )}
              />
              {makeActiveStep(
                2,
                expanded,
                'Finish',
                'Choose a step',
                activeStep
              )}
            </>
          );
        } else {
          return (
            <>
              {UISteps.map((s, idx) => {
                const isActive = idx === activeIndex;
                const hasAddStep = isAdding && activeIndex === idx + 1;
                const isAfterActiveAddStep = activeIndex - 1 < idx;
                const position =
                  isAdding && isAfterActiveAddStep ? idx + 2 : idx + 1;

                return (
                  <React.Fragment key={idx}>
                    <IntegrationFlowStepWithOverview
                      icon={
                        <EntityIcon
                          alt={s.name}
                          entity={s}
                          width={24}
                          height={24}
                        />
                      }
                      i18nTitle={`${position}. ${s.name}`}
                      i18nTooltip={`${position}. ${s.title}`}
                      active={
                        isAdding ? isActive && !isAfterActiveAddStep : isActive
                      }
                      showDetails={expanded}
                      name={s.name}
                      action={s.action && s.action.name!}
                      dataType={
                        idx === 0
                          ? getDataShapeText(s.stepKind!, s.outputDataShape)
                          : getDataShapeText(s.stepKind!, s.inputDataShape)
                      }
                    />
                    {hasAddStep
                      ? makeActiveStep(
                          position + 1,
                          expanded,
                          'Set up this step',
                          'Choose a step',
                          activeStep
                        )
                      : null}
                  </React.Fragment>
                );
              })}
            </>
          );
        }
      }}
    </IntegrationVerticalFlow>
  );
};
