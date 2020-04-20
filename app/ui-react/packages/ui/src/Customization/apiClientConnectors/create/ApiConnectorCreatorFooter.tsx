import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink, Loader } from '../../../Layout';

export interface IApiConnectorCreatorFooterProps {
  backHref: H.LocationDescriptor;
  i18nBack: string;
  i18nReviewEdit?: string;
  i18nNext: string;
  isNextDisabled: boolean;
  isNextLoading: boolean;
  onNext?: (param: any) => void;
  nextHref?: H.LocationDescriptor;
  reviewEditHref?: H.LocationDescriptor;
}

export const ApiConnectorCreatorFooter: React.FunctionComponent<IApiConnectorCreatorFooterProps> = (
  {
    backHref,
    i18nBack,
    i18nReviewEdit,
    i18nNext,
    isNextLoading,
    isNextDisabled,
    onNext,
    nextHref,
    reviewEditHref
  }) => (
  <>
    <div>
      <ButtonLink href={backHref} data-testid={'api-connector-details-form-cancel-button'}>
        {i18nBack}
      </ButtonLink>
      {reviewEditHref && (
        <>
          &nbsp;&nbsp;&nbsp;
          <ButtonLink
            href={reviewEditHref}
            data-testid={
              'api-connector-details-form-review-edit-button'
            }
          >
            {i18nReviewEdit}
          </ButtonLink>
        </>
      )}
      &nbsp;
      <ButtonLink
        as={'primary'}
        disabled={isNextDisabled}
        onClick={onNext}
        href={nextHref}
        data-testid={'api-connector-details-form-save-button'}
      >
        {(isNextLoading) && (
          <Loader size={'sm'} inline={true} />
        )}
        {i18nNext}
      </ButtonLink>
    </div>
  </>
);
