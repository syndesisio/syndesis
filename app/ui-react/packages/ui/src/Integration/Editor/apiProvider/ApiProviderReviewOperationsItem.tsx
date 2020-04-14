import {
  DataListAction,
  DataListCell,
  DataListItem,
  DataListItemCells,
  DataListItemRow,
  Stack,
  StackItem,
} from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { ButtonLink } from '../../../Layout';
import { HttpMethodColors } from '../../../Shared';

import './ApiProviderReviewOperationsItem.css';

export interface IApiProviderReviewOperationsItemProps {
  createAsPrimary: boolean;
  createFlowHref: H.LocationDescriptor;
  i18nCreateFlow: string;
  onCreateFlow?: (e: React.MouseEvent<any>) => void;
  operationDescription: string;
  operationHttpMethod: string;
  operationPath: string;
}

export const ApiProviderReviewOperationsItem: React.FunctionComponent<IApiProviderReviewOperationsItemProps> = ({
  createAsPrimary,
  createFlowHref,
  i18nCreateFlow,
  onCreateFlow,
  operationDescription,
  operationHttpMethod,
  operationPath,
}) => (
  <DataListItem
    aria-labelledby={'method'}
    className={'api-provider-review-operations-item'}
  >
    <DataListItemRow>
      <DataListItemCells
        dataListCells={[
          <DataListCell key={0}>
            <Stack id={'method'}>
              <StackItem>
                <HttpMethodColors method={operationHttpMethod} />
              </StackItem>
              <StackItem>
                <div className={'item__operation-path'}>{operationPath}</div>
              </StackItem>
            </Stack>
          </DataListCell>,
          <DataListCell key={1}>{operationDescription}</DataListCell>,
        ]}
      />
      <DataListAction
        aria-label={'method actions'}
        aria-labelledby={'open-flow-button'}
        id={'method-actions'}
      >
        <ButtonLink
          data-testid={'api-provider-operations-create-flow'}
          onClick={onCreateFlow}
          href={createFlowHref}
          id={'open-flow-button'}
          as={createAsPrimary ? 'primary' : 'default'}
        >
          {i18nCreateFlow}
        </ButtonLink>
      </DataListAction>
    </DataListItemRow>
  </DataListItem>
);
