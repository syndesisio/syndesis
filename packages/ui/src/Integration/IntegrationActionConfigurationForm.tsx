import * as H from 'history';
import * as React from 'react';
import { Link } from 'react-router-dom';

export interface IIntegrationActionConfigurationFormProps {
  backLink: H.LocationDescriptor;
  fields: JSX.Element;
  handleSubmit: (e?: any) => void;
  i18nBackLabel: string;
  i18nSubmitLabel: string;
}

export const IntegrationActionConfigurationForm: React.FunctionComponent<
  IIntegrationActionConfigurationFormProps
> = ({ backLink, handleSubmit, fields, i18nSubmitLabel, i18nBackLabel }) => (
  <form
    className="form-horizontal required-pf"
    role="form"
    onSubmit={handleSubmit}
  >
    <div className={'container-fluid'}>
      <div className="row row-cards-pf">
        <div className="card-pf">
          <div className="card-pf-body">{fields}</div>
          <div className="card-pf-footer">
            <div className="card-pf-time-frame-filter">
              <button type={'submit'} className={'btn btn-primary'}>
                {i18nSubmitLabel}
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
