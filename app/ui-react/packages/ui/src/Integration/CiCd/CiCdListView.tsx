import { Button, ButtonVariant } from '@patternfly/react-core';
import * as React from 'react';
import { PageSection } from '../../Layout';
import { IListViewToolbarProps, ListViewToolbar } from '../../Shared';

export interface ICiCdListViewProps extends IListViewToolbarProps {
  i18nAddNewButtonText: string;
  onAddNew: () => void;
}

export const CiCdListView: React.FunctionComponent<ICiCdListViewProps> = props => {
  return (
    <PageSection>
      <ListViewToolbar {...props}>
        <div className="form-group">
          {props.resultsCount !== 0 && (
            <Button
              data-testid={'cicd-list-view-add-new-button'}
              variant={ButtonVariant.primary}
              onClick={props.onAddNew}
            >
              {props.i18nAddNewButtonText}
            </Button>
          )}
        </div>
      </ListViewToolbar>
      {props.children}
    </PageSection>
  );
};
