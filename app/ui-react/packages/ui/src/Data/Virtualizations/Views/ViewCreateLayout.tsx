// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import {
  Card,
  Grid,
  GridItem,
  Text,
  TextContent,
  TextVariants,
} from '@patternfly/react-core';
import { Table, TableBody, TableHeader, TableVariant } from '@patternfly/react-table';
import * as React from 'react';
import { PageSection } from '../../../Layout';
import './ViewCreateLayout.css';

/**
 * @param header - Header Component for the Create View.
 * @param content - the main content of the wizard. In case of overflow, only
 * the body will scroll.
 * @param preview - The Preview section for selected connection on right side of page.
 */
export interface IViewCreateLayoutProps {
  header: JSX.Element;
  content: JSX.Element;
  selectedTables?: JSX.Element;
  showPreviewData?: boolean;
}

export const ViewCreateLayout: React.FunctionComponent<IViewCreateLayoutProps> = ({
  header,
  content,
  selectedTables,
  showPreviewData
}: IViewCreateLayoutProps) => {

  const columns = [
    { title: 'Repositories' },
    'Branches',
    { title: 'Pull requests'},
    'Workspaces',
    'Last Commit'
  ];
  const rows = [['one', 'two', 'a', 'four', 'five'], ['a', 'two', 'k', 'four', 'five'], ['p', 'two', 'b', 'four', 'five']];

  return (
    <div className={'view-create-layout'}>
      <div className={'view-create-layout__header'}>{header}</div>
      <div className={'view-create-layout__body'}>
        <div className={'view-create-layout__contentOuter'}>
          <div className={'view-create-layout__contentInner'}>
            <PageSection>
              <Card className={'view-create-layout__card'}>
                {selectedTables ? (
                  <>
                    <Grid className={showPreviewData ? 'view-create-layout__grid view-create-layout__grid_withDataPreview' : 'view-create-layout__grid'}>
                      <GridItem
                        span={9}
                        className={'view-create-layout_connection'}
                      >
                        {content}
                      </GridItem>
                      <GridItem
                        span={3}
                        className={'view-create-layout_tableSelected'}
                      >
                        {selectedTables}
                      </GridItem>
                    </Grid>
                    {
                      showPreviewData ? (
                        <Grid className={'view-create-layout__previewData'}>
                      <GridItem
                        span={12}
                        className={'view-create-layout_previewSection'}
                      >
                        <TextContent>
                          <Text component={TextVariants.h2}>
                            <span>Preview</span>
                            <Table
                              variant={TableVariant.compact}
                              cells={columns}
                              rows={rows}
                            >
                              <TableHeader />
                              <TableBody />
                            </Table>
                          </Text>
                        </TextContent>
                      </GridItem>
                    </Grid>
                      ) : ('')
                    }
                  </>
                ) : (
                  <Grid className={'view-create-layout__grid'}>
                    <GridItem span={12}>{content}</GridItem>
                  </Grid>
                )}
              </Card>
            </PageSection>
          </div>
        </div>
      </div>
    </div>
  );
};
