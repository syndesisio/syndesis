import {
  Split,
  Stack,
  TextContent,
  TextList,
  TextListItem,
  TextListItemVariants,
  TextListVariants,
} from '@patternfly/react-core';
import * as React from 'react';
import { PageSection } from '../../../Layout';
import './ApiConnectorDetailHeader.css';

export interface IApiConnectorDetailHeader {
  connectorDescription?: string;
  connectorIcon?: string;
  connectorName?: string;
  /**
   * The localized text of labels
   */
  i18nDescription: string;
  i18nUsageLabel: string;
  i18nUsageMessage: string;
}

export const ApiConnectorDetailHeader: React.FunctionComponent<IApiConnectorDetailHeader> =
  ({
    connectorDescription,
    connectorIcon,
    connectorName,
    i18nDescription,
    i18nUsageLabel,
    i18nUsageMessage,
  }) => {
    return (
      <PageSection variant={'light'}>
        <Stack hasGutter={true}>
          <Split
            hasGutter={true}
            className={'api-connector-detail-header__row'}
          >
            <div>
              <img
                className={'api-connector-detail-header__icon'}
                src={connectorIcon}
              />
            </div>
            <div>{connectorName}</div>
          </Split>
          <TextContent>
            <TextList component={TextListVariants.dl}>
              <TextListItem
                className={'api-connector-detail-header__propertyLabel'}
                component={TextListItemVariants.dt}
              >
                {i18nDescription}
              </TextListItem>
              <TextListItem component={TextListItemVariants.dd}>
                {connectorDescription}
              </TextListItem>
              <TextListItem
                className={'api-connector-detail-header__propertyLabel'}
                component={TextListItemVariants.dt}
              >
                {i18nUsageLabel}
              </TextListItem>
              <TextListItem component={TextListItemVariants.dd}>
                {i18nUsageMessage}
              </TextListItem>
            </TextList>
          </TextContent>
        </Stack>
      </PageSection>
    );
  };
