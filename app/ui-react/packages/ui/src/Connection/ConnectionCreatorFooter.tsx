import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink, Loader } from '../Layout';

export interface IConnectionCreatorFooterProps {
  backHref: H.LocationDescriptor;
  cancelHref?: H.LocationDescriptor;

  i18nBack: string;
  i18nCancel: string;
  i18nSave: string;
  i18nNext: string;

  isLastStep: boolean;
  isNextDisabled: boolean;
  isNextLoading: boolean;

  onNext: (e: React.MouseEvent<any>) => void;
}

export const ConnectionCreatorFooter: React.FunctionComponent<IConnectionCreatorFooterProps> = ({
  backHref,
  cancelHref,
  i18nBack,
  i18nCancel,
  i18nSave,
  i18nNext,
  isLastStep,
  isNextLoading,
  isNextDisabled,
  onNext,
}) => (
  <>
    <ButtonLink
      data-testid={'connection-creator-layout-next-button'}
      onClick={onNext}
      as={'primary'}
      className={'wizard-pf-next'}
      disabled={isNextLoading || isNextDisabled}
    >
      {isNextLoading && (
        <Loader
          size={'xs'}
          inline={true}
          data-testid={'connection-creator-layout-loading'}
        />
      )}
      {isLastStep ? <>{i18nSave}</> : <>{i18nNext}</>}
    </ButtonLink>
    <ButtonLink
      data-testid={'connection-creator-layout-back-button'}
      href={backHref}
      className={'wizard-pf-back'}
    >
      {i18nBack}
    </ButtonLink>
    {cancelHref ? (
      <ButtonLink
        data-testid={'connection-creator-layout-cancel-button'}
        href={cancelHref}
        as={'link'}
        className={'wizard-pf-cancel'}
      >
        {i18nCancel}
      </ButtonLink>
    ) : null}
  </>
);
