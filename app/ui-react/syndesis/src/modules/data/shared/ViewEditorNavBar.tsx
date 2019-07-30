import { RestDataService, ViewDefinition } from '@syndesis/models/src';
import { Container, TabBar, TabBarItem } from '@syndesis/ui';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import resolvers from '../resolvers';

/**
 * @param virtualizationId - the virtualization id for the view
 * @param viewDefinition - the view definition
 * @param previewExpanded - 'true' if the preview are is to be expanded
 */

export interface IViewEditorNavBarProps {
  virtualization: RestDataService;
  viewDefinition: ViewDefinition;
  previewExpanded: boolean;
}

/**
 * A component that displays a nav bar with 4 items:
 *
 * 1. a link to the page that displays View Output
 * 2. a link to the page that displays Join / Union
 * 3. a link to the page that displays View Criteria
 * 4. a link to the page that displays GroupBy
 * 5. a link to the page that displays Properties
 * 6. a link to the page that displays SQL
 *
 */
export const ViewEditorNavBar: React.FunctionComponent<
  IViewEditorNavBarProps
> = props => {

  const { t } = useTranslation(['data', 'shared']);
  const virtualization = props.virtualization;
  const viewDefinition = props.viewDefinition;
  const previewExpanded = props.previewExpanded;

  return (
    <Container
      style={{
        background: '#fff',
      }}
    >
      <TabBar>
        <TabBarItem
          label={t('data:virtualization.viewEditor.viewOutputTab')}
          to={resolvers.virtualizations.views.edit.viewOutput({
            virtualization,
            // tslint:disable-next-line: object-literal-sort-keys
            viewDefinition,
            previewExpanded,
          })}
        />
        <TabBarItem
          label={t('data:virtualization.viewEditor.sqlTab')}
          to={resolvers.virtualizations.views.edit.sql({
            virtualization,
            // tslint:disable-next-line: object-literal-sort-keys
            viewDefinition,
            previewExpanded,
          })}
        />
      </TabBar>
    </Container>
  );
}
