import { Loader, UnrecoverableError } from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';
import { LoadingComponentProps } from 'react-loadable';

export class ModuleLoader extends React.Component<LoadingComponentProps> {
  public render() {
    if (this.props.error || this.props.timedOut) {
      console.error(this.props.error); // tslint:disable-line
      return (
        <Translation ns={['shared']}>
          {t => (
            <UnrecoverableError
              i18nTitle={t('error.title')}
              i18nInfo={t('error.info')}
              i18nHelp={t('error.help')}
              i18nRefreshLabel={t('error.refreshButton')}
              i18nReportIssue={t('shared:error.reportIssueButton')}
              i18nShowErrorInfoLabel={t('error.showErrorInfoButton')}
              error={this.props.error}
            />
          )}
        </Translation>
      );
    } else if (this.props.pastDelay) {
      return <Loader />;
    }
    return null;
  }
}
