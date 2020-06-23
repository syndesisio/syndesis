import {
  TextContent,
  TextList,
  TextListItem,
  TextListItemVariants,
  TextListVariants,
} from '@patternfly/react-core';
import * as React from 'react';
import { Container } from '../../../Layout';
import { IApiConnectorDetailValues } from './ApiConnectorDetailBody';

export interface IApiConnectorDetailConfig {
  i18nLabelBaseUrl: string;
  i18nLabelDescription: string;
  i18nLabelHost: string;
  i18nLabelName: string;

  // Initial Values
  properties: IApiConnectorDetailValues;
}

export const ApiConnectorDetailConfig: React.FunctionComponent<IApiConnectorDetailConfig> = ({
  i18nLabelBaseUrl,
  i18nLabelDescription,
  i18nLabelHost,
  i18nLabelName,
  properties,
}) => {
  // tslint:disable:no-console

  return (
    <>
      <TextContent>
        <Container>
          <TextList component={TextListVariants.dl}>
            {properties.name && (
              <>
                <TextListItem component={TextListItemVariants.dt}>
                  {i18nLabelName}
                </TextListItem>
                <TextListItem component={TextListItemVariants.dd}>
                  {properties.name}
                </TextListItem>
              </>
            )}
            {properties.description && (
              <>
                <TextListItem component={TextListItemVariants.dt}>
                  {i18nLabelDescription}
                </TextListItem>
                <TextListItem component={TextListItemVariants.dd}>
                  {properties.description}
                </TextListItem>
              </>
            )}
            {properties.host && (
              <>
                <TextListItem component={TextListItemVariants.dt}>
                  {i18nLabelHost}
                </TextListItem>
                <TextListItem component={TextListItemVariants.dd}>
                  {properties.host}
                </TextListItem>
              </>
            )}
            {properties.basePath && (
              <>
                <TextListItem component={TextListItemVariants.dt}>
                  {i18nLabelBaseUrl}
                </TextListItem>
                <TextListItem component={TextListItemVariants.dd}>
                  {properties.basePath}
                </TextListItem>
              </>
            )}
          </TextList>
        </Container>
      </TextContent>
    </>
  );
};
