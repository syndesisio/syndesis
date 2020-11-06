import {
  TextContent,
  TextList,
  TextListItem,
  TextListItemVariants,
  TextListVariants,
} from '@patternfly/react-core';
import * as React from 'react';
import { IApiConnectorDetailValues } from './ApiConnectorDetailBody';

export interface IApiConnectorDetailConfig {
  i18nLabelAddress: string;
  i18nLabelBaseUrl: string;
  i18nLabelDescription: string;
  i18nLabelHost: string;
  i18nLabelName: string;

  /**
   * Initial values displayed,
   * typically set when creating the connector
   */
  properties: IApiConnectorDetailValues;

  /**
   * An array of strings with possible properties
   */
  propertyKeys: string[];
}

export const ApiConnectorDetailConfig: React.FunctionComponent<IApiConnectorDetailConfig> = ({
  i18nLabelAddress,
  i18nLabelBaseUrl,
  i18nLabelDescription,
  i18nLabelHost,
  i18nLabelName,
  properties,
  propertyKeys,
}) => {
  return (
    <TextContent data-testid={'api-connector-detail-config'}>
      <TextList component={TextListVariants.dl}>
        {properties.name && (
          <>
            <TextListItem component={TextListItemVariants.dt}>
              {i18nLabelName}
            </TextListItem>
            <TextListItem
              component={TextListItemVariants.dd}
              data-testid={'api-connector-detail-config-name'}
            >
              {properties.name}
            </TextListItem>
          </>
        )}
        {properties.description && (
          <>
            <TextListItem component={TextListItemVariants.dt}>
              {i18nLabelDescription}
            </TextListItem>
            <TextListItem
              component={TextListItemVariants.dd}
              data-testid={'api-connector-detail-config-description'}
            >
              {properties.description}
            </TextListItem>
          </>
        )}
        {propertyKeys.includes('address') && (
          <>
            <TextListItem component={TextListItemVariants.dt}>
              {i18nLabelAddress}
            </TextListItem>
            <TextListItem
              component={TextListItemVariants.dd}
              data-testid={'api-connector-detail-config-address'}
            >
              {properties.address}
            </TextListItem>
          </>
        )}
        {propertyKeys.includes('host') && (
          <>
            <TextListItem component={TextListItemVariants.dt}>
              {i18nLabelHost}
            </TextListItem>
            <TextListItem
              component={TextListItemVariants.dd}
              data-testid={'api-connector-detail-config-host'}
            >
              {properties.host}
            </TextListItem>
          </>
        )}
        {propertyKeys.includes('basePath') && (
          <>
            <TextListItem component={TextListItemVariants.dt}>
              {i18nLabelBaseUrl}
            </TextListItem>
            <TextListItem
              component={TextListItemVariants.dd}
              data-testid={'api-connector-detail-config-path'}
            >
              {properties.basePath}
            </TextListItem>
          </>
        )}
      </TextList>
    </TextContent>
  );
};
