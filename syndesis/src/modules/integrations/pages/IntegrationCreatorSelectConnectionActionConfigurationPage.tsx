import { WithConnection } from '@syndesis/api';
import { AutoForm, IFormDefinition } from '@syndesis/auto-form';
import { Breadcrumb, Loader, PageHeader } from '@syndesis/ui';
import { WithLoader, WithRouter } from '@syndesis/utils';
import { reverse } from 'named-urls';
import * as React from 'react';
import { Link } from 'react-router-dom';

export class IntegrationCreatorSelectConnectionActionConfigurationPage extends React.Component {
  public onSave() {}

  public render() {
    return (
      <WithRouter>
        {({ match, history }) => (
          <WithConnection id={(match.params as any).connectionId}>
            {({ data, hasData, error }) => (
              <WithLoader
                error={error}
                loading={!hasData}
                loaderChildren={<Loader />}
                errorChildren={<div>TODO</div>}
              >
                {() => {
                  const currentStep = (match.params as any).step || 0;
                  const action = data.getActionById(
                    (match.params as any).actionId
                  );
                  const steps = data.getActionSteps(action);
                  const step = data.getActionStep(action, currentStep);
                  const definition = data.getActionStepDefinition(step);
                  const moreSteps = currentStep < steps.length - 1;
                  const onSubmit = () => {
                    if (moreSteps) {
                      history.push(
                        reverse(match.path, {
                          ...match.params,
                          step: currentStep + 1,
                        })
                      );
                    } else {
                      // TODO save
                    }
                  };
                  return (
                    <>
                      <PageHeader>
                        <Breadcrumb>
                          <Link to={'/'}>Home</Link>
                          <Link to={'/integrations'}>Integrations</Link>
                          <Link to={'/integrations/create'}>
                            New integration
                          </Link>
                          <Link to={`/integrations/create/${data.id}`}>
                            {data.name}
                          </Link>
                          <span>{action.name}</span>
                        </Breadcrumb>

                        <h1>{action.name}</h1>
                        <p>{action.description}</p>
                      </PageHeader>
                      <AutoForm
                        i18nRequiredProperty={'* Required field'}
                        definition={definition as IFormDefinition}
                        initialValue={{}}
                        onSave={onSubmit}
                      >
                        {({ fields, handleSubmit }) => (
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
                                    <Link
                                      to={`/integrations/create/${data.id}`}
                                      className={'btn btn-default'}
                                    >
                                      &lt; Choose action
                                    </Link>
                                    <button
                                      type={'submit'}
                                      className={'btn btn-primary'}
                                    >
                                      {moreSteps ? 'Continue' : 'Done'}
                                    </button>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </form>
                        )}
                      </AutoForm>
                    </>
                  );
                }}
              </WithLoader>
            )}
          </WithConnection>
        )}
      </WithRouter>
    );
  }
}
