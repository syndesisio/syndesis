import { Step } from '@syndesis/models';
import {
  IntegrationFlowAddStep,
  IntegrationFlowStepGeneric,
  IntegrationFlowStepWithOverview,
  IntegrationVerticalFlow,
} from '@syndesis/ui';
import * as H from 'history';
import * as React from 'react';

export interface IIntegrationEditorSidebarProps {
  disabled?: boolean;
  steps: Step[];
  activeIndex?: number;
  addAtIndex?: number;
  addType?: 'connection' | 'step';
  addI18nTitle?: string;
  addI18nTooltip?: string;
  addI18nDescription?: string;
  forceTooltips?: boolean;
  addConnectionHref?(idx: number): H.LocationDescriptor;
  addStepHref?(idx: number): H.LocationDescriptor;
  configureConnectionHref(idx: number, step: Step): H.LocationDescriptor;
  configureStepHref(idx: number, step: Step): H.LocationDescriptor;
}

export class IntegrationEditorSidebar extends React.Component<
  IIntegrationEditorSidebarProps
> {
  public static defaultProps = {
    disabled: false,
    forceTooltips: false,
  };

  public render() {
    return (
      <IntegrationVerticalFlow disabled={this.props.disabled}>
        {({ expanded }) =>
          this.props.steps.map((s, idx) => {
            const hasAddStep = idx < this.props.steps.length - 1;
            const addStep = (
              <IntegrationFlowAddStep
                forceTooltip={this.props.forceTooltips}
                showDetails={expanded}
                addStepHref={
                  this.props.disabled
                    ? undefined
                    : this.props.addStepHref!(idx + 1)
                }
                i18nAddStep={'Add a step'}
                addConnectionHref={
                  this.props.disabled
                    ? undefined
                    : this.props.addConnectionHref!(idx + 1)
                }
                i18nAddConnection={'Add a connection'}
              />
            );
            const activeAddStep = (
              <IntegrationFlowStepGeneric
                icon={<i className={'fa fa-plus'} />}
                i18nTitle={this.props.addI18nTitle!}
                i18nTooltip={this.props.addI18nTooltip!}
                active={true}
                showDetails={expanded}
                description={this.props.addI18nDescription!}
              />
            );
            return (
              <React.Fragment key={idx}>
                {s.stepKind === 'endpoint' && (
                  <IntegrationFlowStepWithOverview
                    icon={
                      <img src={s.connection!.icon} width={24} height={24} />
                    }
                    i18nTitle={`${idx + 1}. ${s.action!.name}`}
                    i18nTooltip={`${idx + 1}. ${s.action!.name}`}
                    active={false}
                    showDetails={expanded}
                    name={s.connection!.connector!.name}
                    action={s.action!.name}
                    dataType={'TODO'}
                    href={
                      this.props.disabled
                        ? undefined
                        : this.props.configureConnectionHref(idx, s)
                    }
                  />
                )}
                {hasAddStep &&
                  (this.props.addAtIndex! - 1 === idx
                    ? activeAddStep
                    : addStep)}
              </React.Fragment>
            );
          })
        }
      </IntegrationVerticalFlow>
    );
  }
}
