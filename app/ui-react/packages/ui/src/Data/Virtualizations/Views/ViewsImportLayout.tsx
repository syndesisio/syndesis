// tslint:disable react-unused-props-and-state
// remove the above line after this goes GA https://github.com/Microsoft/tslint-microsoft-contrib/pull/824
import {
  Card,
  Grid,
  GridItem,
} from '@patternfly/react-core';
import * as React from 'react';
import { PageSection } from '../../../Layout';
import './ViewsImportLayout.css';

/**
 * @param header - Header Component for Views import.
 * @param content - the main content of the wizard. In case of overflow, only
 * the body will scroll.
 */

export interface IViewsImportLayoutProps {
  header: JSX.Element;
  content: JSX.Element;
}

export const ViewsImportLayout: React.FunctionComponent<IViewsImportLayoutProps> = ({
  header,
  content,
}: IViewsImportLayoutProps) => {

  return (
    <div className={'views-import-layout'}>
      <div className={'views-import-layout__header'}>{header}</div>
      <div className={'views-import-layout__body'}>
        <div className={'views-import-layout__contentOuter'}>
          <div className={'views-import-layout__contentInner'}>
            <PageSection>
              <Card className={'views-import-layout__card'}>
                <Grid className={'views-import-layout__grid'}>
                  <GridItem span={12}>{content}</GridItem>
                </Grid>
              </Card>
            </PageSection>
          </div>
        </div>
      </div>
    </div>
  );
};
