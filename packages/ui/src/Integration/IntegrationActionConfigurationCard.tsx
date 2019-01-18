import * as H from 'history';
import * as React from 'react';
import { Link } from 'react-router-dom';
import { Loader } from '../Shared';

export interface IIntegrationActionConfigurationFormProps {
  backLink: H.LocationDescriptor;
  content: JSX.Element;
  onSubmit: (e?: any) => void;
  i18nBackLabel: string;
  i18nSubmitLabel: string;
  disabled?: boolean;
}

export const IntegrationActionConfigurationCard: React.FunctionComponent<
  IIntegrationActionConfigurationFormProps
> = ({
  backLink,
  onSubmit,
  content,
  i18nSubmitLabel,
  i18nBackLabel,
  disabled,
}) => (
  <form className="form-horizontal required-pf" role="form" onSubmit={onSubmit}>
    <div className={'container-fluid'}>
      <div className="row row-cards-pf">
        <div className="card-pf">
          <div className="card-pf-body">{content}</div>
          <div className="card-pf-footer">
            <div className="card-pf-time-frame-filter">
              <button
                type={'submit'}
                className={'btn btn-primary'}
                disabled={disabled}
              >
                {i18nSubmitLabel}{' '}
                {disabled && <Loader size={'xs'} inline={true} />}
              </button>
            </div>
            <Link to={backLink} className={'btn btn-default'}>
              {i18nBackLabel}
            </Link>
          </div>
        </div>
      </div>
    </div>
  </form>
);
