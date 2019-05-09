import {
  WithApiVersion,
  WithIntegrationHelpers,
  WithIntegrations,
} from '@syndesis/api';
import {
  DownloadDiagnostics,
  Loader,
  PageSection,
  SimplePageHeader,
  SupportPageBody,
} from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { UIContext } from '../../../app/UIContext';
import { ApiError, PageTitle } from '../../../shared';
import { SelectiveIntegrationList } from '../components/SelectiveIntegrationList';

export const getNames = (integrations: any) =>
  integrations.reduce((acc: object[], curVal: any) => {
    acc[`${curVal.name}`] = true;
    return acc;
  }, {});

export const SupportPage: React.FunctionComponent = () => {
  const [selectedLogType, setSelectedLogType] = React.useState('alllogs');
  const [integrationsToDl, setIntegrationsToDl] = React.useState({});
  const handleLogTypeChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSelectedLogType(event.target.value);
  };
  return (
    <>
      <PageTitle title="Support" />
      <SimplePageHeader
        i18nTitle={'Support'}
        i18nDescription={`To obtain support, download diagnostic information through this page and open a request on the <a href="https://access.redhat.com/support/cases/#/case/new">Red Hat Customer portal</a>. If you have any issues please see the support <a href="https://access.redhat.com/solutions/2112">instructions</a>.`}
      />
      <PageSection>
        <h2 className="pf-c-title pf-m-lg">Version</h2>
        <WithApiVersion>
          {({ data, error, loading }) => {
            const {
              'commit-id': commitId,
              'build-id': buildId,
              version,
            } = data;
            return (
              <WithLoader
                error={error}
                loading={loading}
                loaderChildren={<Loader />}
                errorChildren={<ApiError />}
              >
                {() => (
                  <SupportPageBody
                    i18nProductName={'Syndesis'}
                    version={version}
                    buildId={buildId}
                    commitId={commitId}
                  />
                )}
              </WithLoader>
            );
          }}
        </WithApiVersion>
      </PageSection>

      <PageSection variant="light">
        <WithIntegrationHelpers>
          {({ downloadSupportData }) => {
            return (
              <WithIntegrations>
                {({ data, error, loading }) => {
                  return (
                    <>
                      <WithLoader
                        error={error}
                        loading={loading}
                        loaderChildren={<Loader />}
                        errorChildren={<ApiError />}
                      >
                        {() => {
                          const handleIntegrationChecked = (
                            event: React.ChangeEvent<HTMLInputElement>
                          ) => {
                            if (event.target.value in integrationsToDl) {
                              const filteredIntegrations = integrationsToDl;
                              delete filteredIntegrations[event.target.value];
                              setIntegrationsToDl({ ...filteredIntegrations });
                            } else {
                              const newIntegrations = Object.assign(
                                { [`${event.target.value}`]: true },
                                integrationsToDl
                              );
                              setIntegrationsToDl(newIntegrations);
                            }
                          };

                          return (
                            <DownloadDiagnostics>
                              <div className="support-form pf-u-my-xl">
                                <h3 className="pf-c-title pf-m-sm">
                                  Select the integrations to capture diagnostics
                                  for:
                                </h3>
                                <div className="radio">
                                  <label htmlFor="alllogs">
                                    <input
                                      type="radio"
                                      id="alllogs"
                                      value="alllogs"
                                      checked={selectedLogType === 'alllogs'}
                                      onChange={event => {
                                        handleLogTypeChange(event);
                                        setIntegrationsToDl(
                                          getNames(data.items)
                                        );
                                      }}
                                      name="logs"
                                    />
                                    All Integrations
                                  </label>
                                </div>
                                <div className="radio">
                                  <label htmlFor="specificlogs">
                                    <input
                                      type="radio"
                                      id="specificlogs"
                                      value="specificlogs"
                                      checked={
                                        selectedLogType === 'specificlogs'
                                      }
                                      onChange={event => {
                                        handleLogTypeChange(event);
                                        setIntegrationsToDl({});
                                      }}
                                      name="logs"
                                    />
                                    Specific Integrations
                                  </label>
                                </div>

                                {selectedLogType === 'specificlogs' && (
                                  <SelectiveIntegrationList
                                    data={data}
                                    onIntegrationChecked={
                                      handleIntegrationChecked
                                    }
                                  />
                                )}

                                <UIContext.Consumer>
                                  {({ pushNotification }) => {
                                    const isEmpty = (integrationsObj: object) =>
                                      Object.keys(integrationsObj).length === 0;
                                    const handleDownload = (
                                      evt: React.MouseEvent<HTMLButtonElement>
                                    ) => {
                                      evt.preventDefault();
                                      if (isEmpty(integrationsToDl)) {
                                        downloadSupportData(
                                          getNames(data.items)
                                        );
                                      } else {
                                        downloadSupportData(integrationsToDl);
                                      }
                                      pushNotification(
                                        'Generating Troubleshooting Diagnostics For Download',
                                        'info'
                                      );
                                    };
                                    return (
                                      <button
                                        className="btn btn-primary"
                                        disabled={
                                          isEmpty(integrationsToDl) &&
                                          selectedLogType === 'specificlogs'
                                        }
                                        onClick={handleDownload}
                                      >
                                        Download
                                      </button>
                                    );
                                  }}
                                </UIContext.Consumer>
                              </div>
                            </DownloadDiagnostics>
                          );
                        }}
                      </WithLoader>
                    </>
                  );
                }}
              </WithIntegrations>
            );
          }}
        </WithIntegrationHelpers>
      </PageSection>
    </>
  );
};
