import {
  Split,
  SplitItem,
  Text,
  TextContent,
  TextVariants,
} from '@patternfly/react-core';
import { LongArrowAltRightIcon } from '@patternfly/react-icons';
import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink, Loader } from '../../../Layout';
import './ViewWizardHeader.css';

/**
 * @param onCancel - if passed, the Cancel button will be render as a `button`
 * and this callback will be used as its `onClick` handler.
 * @param onBack - if passed, the Back button will be render as a `button`
 * and this callback will be used as its `onClick` handler.
 * @param onNext - if passed, the Next button will be render as a `button`
 * and this callback will be used as its `onClick` handler.
 * @param cancelHref - if passed, the Cancel button will be render as a `Link`
 * using this as its `to` parameter.
 * @param backHref - if passed, the Back button will be render as a `Link`
 * using this as its `to` parameter.
 * @param nextHref - if passed, the Next button will be render as a `Link`
 * using this as its `to` parameter.
 * @param isNextLoading - if set to true, a `Loading` component will be shown
 * inside the Next button before its label. The button will also be disabled.
 * @param isNextDisabled - if set to true, the Next button will be disabled.
 * @param isLastStep - if set to true, it changes the Next button label to
 * 'Done'.
 */

export interface IViewWizardHeaderProps {
  /**
   * The one-based active step number.
   */
  step: number;
  i18nStep1Text: string;
  i18nStep2Text: string;
  i18nBack: string;
  i18nDone: string;
  i18nNext: string;
  i18nCancel: string;
  onCancel?: (e: React.MouseEvent<any>) => void;
  onBack?: (e: React.MouseEvent<any>) => void;
  onNext?: (e: React.MouseEvent<any>) => void;
  cancelHref?: H.LocationDescriptor;
  backHref?: H.LocationDescriptor;
  nextHref?: H.LocationDescriptor;
  isNextDisabled?: boolean;
  isNextLoading?: boolean;
  isLastStep?: boolean;
}

export const ViewWizardHeader: React.FunctionComponent<IViewWizardHeaderProps> = ({
  step,
  i18nStep1Text,
  i18nStep2Text,
  i18nBack,
  i18nDone,
  i18nNext,
  i18nCancel,
  onCancel,
  onBack,
  onNext,
  cancelHref,
  backHref,
  nextHref,
  isNextLoading,
  isNextDisabled,
  isLastStep = false,
}: IViewWizardHeaderProps) => {
  return (
    <Split gutter="md" className={'view_wizard_header__header-space'}>
      <SplitItem>
        <TextContent>
          <Text component={TextVariants.h2}>
            <span
              className={step !== 1 ? 'view_wizard_header__Notselected' : ''}
            >{`1. ${i18nStep1Text}`}</span>
            <LongArrowAltRightIcon color={'#6A6A6A'} className={'view_wizard_header__Wizard-step'}/>
            <span
              className={step !== 2 ? 'view_wizard_header__Notselected' : ''}
            >{`2. ${i18nStep2Text}`}</span>
          </Text>
        </TextContent>
      </SplitItem>
      <SplitItem isFilled={true} />
      <SplitItem>
        {step === 2 && (
          <ButtonLink
            data-testid={'view-wizard-header-back-button'}
            onClick={onBack}
            href={backHref}
            className={'wizard-pf-back'}
          >
            <i className="fa fa-angle-left" /> {i18nBack}
          </ButtonLink>
        )}
        &nbsp;
        <ButtonLink
          data-testid={'view-wizard-header-next-button'}
          onClick={onNext}
          href={nextHref}
          as={'primary'}
          className={'wizard-pf-next'}
          disabled={isNextLoading || isNextDisabled}
        >
          {isNextLoading ? <Loader size={'xs'} inline={true} /> : null}
          {isLastStep ? (
            ` ${i18nDone}`
          ) : (
            <>
              {i18nNext} <i className="fa fa-angle-right" />
            </>
          )}
        </ButtonLink>
        &nbsp;
        <ButtonLink
          data-testid={'view-wizard-header-cancel-button'}
          onClick={onCancel}
          href={cancelHref}
          className={'wizard-pf-cancel'}
        >
          {i18nCancel}
        </ButtonLink>
      </SplitItem>
    </Split>
  );
};
