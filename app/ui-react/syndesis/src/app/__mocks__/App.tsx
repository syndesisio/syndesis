import { ApiContext, ServerEventsContext } from '@syndesis/api';
import * as React from 'react';
import { I18nextProvider } from 'react-i18next';
import i18n from '../../i18n';
import { IAppBaseProps } from '../App';
import { AppContext } from '../AppContext';
import { WithConfig } from '../WithConfig';

export class App extends React.Component<IAppBaseProps> {
  public render() {
    return (
      <I18nextProvider i18n={i18n}>
        <WithConfig>
          {({ config }) => (
            <AppContext.Provider
              value={{
                config: config!,
                getPodLogUrl: () => '',
                user: { username: 'Unknown user' },
              }}
            >
              <ApiContext.Provider
                value={{
                  apiUri: `${config!.apiBase}${config!.apiEndpoint}`,
                  dvApiUri: `${config!.apiBase}${config!.datavirt.dvUrl}`,
                  headers: { 'SYNDESIS-XSRF-TOKEN': 'awesome' },
                }}
              >
                <ServerEventsContext.Provider
                  value={{
                    registerChangeListener: () => void 0,
                    registerMessageListener: () => void 0,
                    unregisterChangeListener: () => void 0,
                    unregisterMessageListener: () => void 0,
                  }}
                >
                  {this.props.children}
                </ServerEventsContext.Provider>
              </ApiContext.Provider>
            </AppContext.Provider>
          )}
        </WithConfig>
      </I18nextProvider>
    );
  }
}
