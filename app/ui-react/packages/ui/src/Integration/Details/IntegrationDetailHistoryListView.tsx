import {
  Card,
  CardBody,
  CardHeader,
  DataList,
  Split,
  SplitItem,
  Stack,
  StackItem,
  Title,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink, PageSection } from '../../Layout';

export interface IIntegrationDetailHistoryListViewProps {
  editHref?: H.LocationDescriptor;
  editLabel?: string | JSX.Element;
  hasHistory: boolean;
  isDraft: boolean;
  i18nTextDraft?: string;
  i18nTextHistory?: string;
  publishAction?: (e: React.MouseEvent<any>) => any;
  publishHref?: H.LocationDescriptor;
  publishLabel?: string | JSX.Element;
}

export const IntegrationDetailHistoryListView: React.FunctionComponent<IIntegrationDetailHistoryListViewProps> =
  ({
    children,
    editHref,
    editLabel,
    hasHistory,
    isDraft,
    i18nTextDraft,
    i18nTextHistory,
    publishAction,
    publishHref,
    publishLabel,
  }) => (
    <PageSection>
      <Stack hasGutter={true}>
        {isDraft && (
          <StackItem>
            <Card>
              <CardBody>
                <Split hasGutter={true}>
                  <SplitItem>
                    <Title headingLevel="h5" size={'md'}>
                      {i18nTextDraft}
                    </Title>
                  </SplitItem>
                  <SplitItem isFilled={true}>&nbsp;</SplitItem>
                  <SplitItem>
                    <ButtonLink
                      data-testid={
                        'integration-detail-history-list-view-publish-button'
                      }
                      to={publishHref}
                      onClick={publishAction}
                      children={publishLabel}
                    />
                    &nbsp; &nbsp;
                    <ButtonLink
                      data-testid={
                        'integration-detail-history-list-view-edit-button'
                      }
                      href={editHref}
                      children={editLabel}
                    />
                  </SplitItem>
                </Split>
              </CardBody>
            </Card>
          </StackItem>
        )}
        {children && hasHistory && (
          <StackItem isFilled={true}>
            <Card>
              <CardHeader>{i18nTextHistory}</CardHeader>
              <CardBody>
                <DataList aria-label={'integration detail history list'}>
                  {children}
                </DataList>
              </CardBody>
            </Card>
          </StackItem>
        )}
      </Stack>
    </PageSection>
  );
