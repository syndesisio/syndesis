import {
  RestDataService,
  ViewDefinition,
} from '@syndesis/models';
import {
  Breadcrumb,
} from '@syndesis/ui';
import { ExpandablePreview, PageSection } from '@syndesis/ui';
import { useRouteData } from '@syndesis/utils';
import * as React from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import resolvers from '../../../resolvers';
import {
  ViewEditorNavBar
} from '../../shared';

/**
 * @param virtualization - the Virtualization
 * @param viewDefinition - the ViewDefinition
 */
export interface IViewEditorOutputRouteState {
  virtualization: RestDataService;
  viewDefinition: ViewDefinition;
  previewExpanded: boolean;
}

export const ViewEditorOutputPage: React.FunctionComponent = () => {

  const { t } = useTranslation(['data', 'shared']);
  const { state } = useRouteData<null, IViewEditorOutputRouteState>();

  const [previewExpanded, setPreviewExpanded] = React.useState(state.previewExpanded);

  const handlePreviewExpandedChanged = (
    expanded: boolean
  ) => {
    setPreviewExpanded(expanded);
  };

  return (
    <>
      <Breadcrumb>
        <Link to={resolvers.dashboard.root()}>
          {t('shared:Home')}
        </Link>
        <Link to={resolvers.data.root()}>
          {t('shared:DataVirtualizations')}
        </Link>
        <Link
          to={resolvers.data.virtualizations.views.root(
            {
              virtualization:state.virtualization,
            }
          )}
        >
          {state.virtualization.keng__id}
        </Link>
        <span>{state.viewDefinition.viewName}</span>
      </Breadcrumb>
      <PageSection variant={'light'} noPadding={true}>
        <ViewEditorNavBar virtualization={state.virtualization} viewDefinition={state.viewDefinition} previewExpanded={previewExpanded} />
      </PageSection>
      <PageSection>View Output Table components go here</PageSection>
      <PageSection variant={'light'} noPadding={true}>
        <ExpandablePreview
          i18nHidePreview={t('data:virtualization.preview.hidePreview')}
          i18nShowPreview={t('data:virtualization.preview.showPreview')}
          initialExpanded={previewExpanded}
          onPreviewExpandedChanged={handlePreviewExpandedChanged}
        />
      </PageSection>
    </>
  );
}
