import { PageSection, UnrecoverableError } from '@syndesis/ui';
import * as React from 'react';
import { Translation } from 'react-i18next';

export const PageNotFound: React.FC = () => (
  <PageSection>
    <Translation ns={['shared']}>
      {t => (
        <UnrecoverableError
          i18nTitle={t('404.title')}
          i18nInfo={t('404.info')}
          i18nHelp={t('404.help')}
          i18nRefreshLabel={t('404.refreshButton')}
          i18nReportIssue={t('404.reportIssueButton')}
        />
      )}
    </Translation>
  </PageSection>
);
