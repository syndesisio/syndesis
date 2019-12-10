import {
  Split,
  SplitItem,
  Text,
  TextContent,
  TextVariants,
} from '@patternfly/react-core';
import { ArrowRightIcon } from '@patternfly/react-icons';
import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink, Loader } from 'src';
import './CreateViewHeader.css';

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

export interface ICreateViewHeaderProps {
  /**
   * The one-based active step number.
   */
  step: number;
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

export const CreateViewHeader: React.FunctionComponent<
  ICreateViewHeaderProps
> = ({
  step,
  onCancel,
  onBack,
  onNext,
  cancelHref,
  backHref,
  nextHref,
  isNextLoading,
  isNextDisabled,
  isLastStep = false,
}: ICreateViewHeaderProps) => {
  return (
    <Split gutter="md" className={'header-space'}>
      <SplitItem>
        <TextContent>
          <Text component={TextVariants.h2}>
            <span className={step !==1 ? 'Notselected' : ''}>{`1.Choose one or more tables `}</span>     
            <ArrowRightIcon />
            <span className={step !==2 ? 'Notselected' : ''}>{` 2. Name your view`}</span>
          </Text>
        </TextContent>
      </SplitItem>
      <SplitItem isFilled={true} />
      <SplitItem>
        <ButtonLink
          data-testid={'view-create-layout-back-button'}
          onClick={onBack}
          href={backHref}
          className={'wizard-pf-back'}
        >
          <i className="fa fa-angle-left" /> Back
        </ButtonLink>
        &nbsp;
        <ButtonLink
          data-testid={'view-create-layout-next-button'}
          onClick={onNext}
          href={nextHref}
          as={'primary'}
          className={'wizard-pf-next'}
          disabled={isNextLoading || isNextDisabled}
        >
          {isNextLoading ? <Loader size={'xs'} inline={true} /> : null}
          {isLastStep ? (
            'Done'
          ) : (
            <>
              Next <i className="fa fa-angle-right" />
            </>
          )}
        </ButtonLink>
        &nbsp;
        <ButtonLink
          data-testid={'view-create-layout-cancel-button'}
          onClick={onCancel}
          href={cancelHref}
          className={'wizard-pf-cancel'}
        >
          Cancel
        </ButtonLink>
      </SplitItem>
    </Split>
  );
};
