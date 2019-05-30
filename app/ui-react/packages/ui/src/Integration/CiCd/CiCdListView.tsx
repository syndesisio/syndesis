import { Button } from 'patternfly-react';
import * as React from 'react';
import { PageSection } from '../../Layout';
import { IListViewToolbarProps, ListViewToolbar } from '../../Shared';

export interface ICiCdListViewProps extends IListViewToolbarProps {
  i18nAddNewButtonText: string;
  onAddNew: () => void;
}

export class CiCdListView extends React.Component<ICiCdListViewProps> {
  public render() {
    return (
      <PageSection>
        <ListViewToolbar {...this.props}>
          <div className="form-group">
            {this.props.resultsCount !== 0 && (
              <Button
                data-testid={'cicd-list-view-add-new-button'}
                className="btn btn-primary"
                onClick={this.props.onAddNew}
              >
                {this.props.i18nAddNewButtonText}
              </Button>
            )}
          </div>
        </ListViewToolbar>
        {this.props.children}
      </PageSection>
    );
  }
}
