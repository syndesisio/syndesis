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
  steps: Step[];
  activeIndex?: number;
  addAtIndex?: number;
  addType?: 'connection' | 'step';
  addIcon?: any;
  addI18nTitle?: string;
  addI18nTooltip?: string;
  addI18nDescription?: string;
  forceTooltips?: boolean;
  addConnectionHref?(idx: number): H.LocationDescriptor;
  addStepHref?(idx: number): H.LocationDescriptor;
  configureConnectionHref(idx: number): H.LocationDescriptor;
  configureStepHref(idx: number): H.LocationDescriptor;
}

export class IntegrationEditorSidebar extends React.Component<
  IIntegrationEditorSidebarProps
> {
  public static defaultProps = {
    forceTooltips: false,
  };

  public render() {
    const disabled = !!this.props.addAtIndex;
    return (
      <IntegrationVerticalFlow disabled={disabled}>
        {({ expanded }) =>
          this.props.steps.map((s, idx) => {
            const hasAddStep = idx < this.props.steps.length - 1;
            const hasActiveAddStep = this.props.addAtIndex! - 1 === idx;
            const isAfterActiveAddStep = this.props.addAtIndex! - 1 < idx;
            const position = isAfterActiveAddStep ? idx + 2 : idx + 1;
            const addStep = (
              <IntegrationFlowAddStep
                forceTooltip={this.props.forceTooltips}
                showDetails={expanded}
                addStepHref={
                  disabled ? undefined : this.props.addStepHref!(position)
                }
                i18nAddStep={'Add a step'}
                addConnectionHref={
                  disabled ? undefined : this.props.addConnectionHref!(position)
                }
                i18nAddConnection={'Add a connection'}
              />
            );
            const activeAddStep = (
              <IntegrationFlowStepGeneric
                icon={this.props.addIcon || <i className={'fa fa-plus'} />}
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
                    i18nTitle={`${position}. ${s.action!.name}`}
                    i18nTooltip={`${position}. ${s.action!.name}`}
                    active={false}
                    showDetails={expanded}
                    name={s.connection!.connector!.name}
                    action={s.action!.name}
                    dataType={'TODO'}
                    href={
                      disabled
                        ? undefined
                        : this.props.configureConnectionHref(idx)
                    }
                  />
                )}
                {hasAddStep && (hasActiveAddStep ? activeAddStep : addStep)}
              </React.Fragment>
            );
          })
        }
      </IntegrationVerticalFlow>
    );
  }
}
