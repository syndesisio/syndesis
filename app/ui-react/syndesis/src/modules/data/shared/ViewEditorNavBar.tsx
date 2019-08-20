import { QueryResults, RestDataService, ViewDefinition } from '@syndesis/models/src';
import { Container, PreviewButtonSelection, TabBar, TabBarItem } from '@syndesis/ui';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import resolvers from '../resolvers';

/**
 * @param virtualizationId - the virtualization id for the view
 * @param viewDefinitionId - the view definition id
 * @param previewExpanded - 'true' if the preview are is to be expanded
 * @param viewDefinition - the view definition (optional)
 * @param previewButtonSelection - the preview button selection 
 * @param queryResults - current query results
 */

export interface IViewEditorNavBarProps {
  virtualization: RestDataService;
  viewDefinitionId: string;
  previewExpanded: boolean;
  viewDefinition?: ViewDefinition;
  previewButtonSelection: PreviewButtonSelection;
  queryResults: QueryResults;
  onEditFinished: () => void;
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
  const viewDefinitionId = props.viewDefinitionId;
  const previewButtonSelection = props.previewButtonSelection;
  const queryResults = props.queryResults;

  const handleEditFinished = async () => {
    props.onEditFinished();
  };

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
            viewDefinitionId,
            previewExpanded,
            viewDefinition,
            previewButtonSelection,
            queryResults,
          })}
        />
        <TabBarItem
          label={t('data:virtualization.viewEditor.sqlTab')}
          to={resolvers.virtualizations.views.edit.sql({
            virtualization,
            // tslint:disable-next-line: object-literal-sort-keys
            viewDefinitionId,
            previewExpanded,
            viewDefinition,
            previewButtonSelection,
            queryResults,
          })}
        />
      </TabBar>
      <button
        data-testid={
          'view-editor-navbar-done-button'
        }
        className="btn btn-default"
        disabled={false}
        onClick={handleEditFinished}
      >
        Done
      </button>
    </Container>
  );
}
