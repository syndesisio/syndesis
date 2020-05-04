import {
  DataList,
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
import { toValidHtmlId } from '../../../helpers';
import { ButtonLink } from '../../../Layout';

export interface IFlowItem {
  condition: string;
  href: H.LocationDescriptor;
}

export interface IChoiceConfigurationViewProps {
  flowItems: IFlowItem[];
  useDefaultFlow: boolean;
  defaultFlowHref?: H.LocationDescriptor;
  i18nOpenFlow: string;
  i18nOtherwise: string;
  i18nUseDefaultFlow: string;
  i18nWhen: string;
}

export const ChoiceConfigurationView: React.FunctionComponent<IChoiceConfigurationViewProps> = ({
  flowItems,
  defaultFlowHref,
  i18nOpenFlow,
  i18nOtherwise,
  i18nUseDefaultFlow,
  i18nWhen,
  useDefaultFlow,
}) => (
  <DataList aria-label={'conditional flow list'}>
    {flowItems.map((item: IFlowItem, index: number) => {
      const id = toValidHtmlId(item.condition);
      return (
        <DataListItem aria-labelledby={id} key={index}>
          <DataListItemRow data-testid={'condition-row'}>
            <DataListItemCells
              dataListCells={[
                <DataListCell key={0} aria-label={'condition list item name'}>
                  <Stack>
                    <StackItem data-testid={'condition-label'}>
                      <b id={id}>{`${index + 1}. ${i18nWhen}`}</b>
                    </StackItem>
                    <StackItem data-testid={'condition-expression'}>
                      {item.condition}
                    </StackItem>
                  </Stack>
                </DataListCell>,
              ]}
            />
            <DataListAction
              id={`${id}-actions`}
              aria-labelledby={id}
              aria-label={`condition ${item.condition} actions`}
            >
              <ButtonLink
                data-testid={`choice-view-mode-item-${toValidHtmlId(
                  item.condition
                )}-view-flow-button`}
                href={item.href}
                as="primary"
              >
                {i18nOpenFlow}
              </ButtonLink>
            </DataListAction>
          </DataListItemRow>
        </DataListItem>
      );
    })}
    {useDefaultFlow && (
      <DataListItem aria-labelledby={'defaultFlow'} key={'defaultFlow'}>
        <DataListItemRow data-testid={'condition-row default-flow-row'}>
          <DataListItemCells
            dataListCells={[
              <DataListCell key={0} aria-label={'condition list item name'}>
                <Stack>
                  <StackItem data-testid={'condition-label'}>
                    <b id={'defaultFlow'}>{i18nOtherwise}</b>
                  </StackItem>
                  <StackItem data-testid={'condition-expression'}>
                    {i18nUseDefaultFlow}
                  </StackItem>
                </Stack>
              </DataListCell>,
            ]}
          />
          <DataListAction
            id={`defaultFlow-actions`}
            aria-labelledby={'defaultFlow'}
            aria-label={`default flow actions`}
          >
            <ButtonLink
              data-testid="choice-view-mode-view-default-flow-button"
              href={defaultFlowHref}
              as="primary"
            >
              {i18nOpenFlow}
            </ButtonLink>
          </DataListAction>
        </DataListItemRow>
      </DataListItem>
    )}
  </DataList>
);
