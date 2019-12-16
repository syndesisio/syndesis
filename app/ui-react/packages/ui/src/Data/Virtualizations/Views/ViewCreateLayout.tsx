// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import { Card, Grid, GridItem } from '@patternfly/react-core';
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
  preview?: JSX.Element;
}

export const ViewCreateLayout: React.FunctionComponent<IViewCreateLayoutProps> = ({
  header,
  content,
  preview,
}: IViewCreateLayoutProps) => {
  return (
    <div className={'virtualization-view-editor-layout'}>
      <div className={'virtualization-view-editor-layout__header'}>
        {header}
      </div>
      <div className={'virtualization-view-editor-layout__body'}>
        <div className={'virtualization-view-editor-layout__contentOuter'}>
          <div className={'virtualization-view-editor-layout__contentInner'}>
            <PageSection>
              <Card className={'virtualization-view-editor-layout__card'}>
                {preview ? (
                  <Grid className={'virtualization-view-editor-layout__grid'}>
                    <GridItem
                      span={9}
                      className={'virtualization-view-editor-layout_connection'}
                    >
                      {content}
                    </GridItem>
                    <GridItem
                      span={3}
                      className={
                        'virtualization-view-editor-layout_previewSection'
                      }
                    >
                      {preview}
                    </GridItem>
                  </Grid>
                ) : (
                  <Grid className={'virtualization-view-editor-layout__grid'}>
                    <GridItem span={12} className={'class1'}>
                      {content}
                    </GridItem>
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
