import { Button, ButtonVariant, Grid, GridItem } from '@patternfly/react-core';
import * as H from '@syndesis/history';
import * as React from 'react';
import { Container, TabBar, TabBarItem } from '../../../Layout'

/**
 * @param i18nFinishButton - i18n text for the finish button
 * @param i18nViewOutputTab - i18n text for the view output tab
 * @param i18nViewSqlTab - i18n text for the view sql tab
 * @param viewOutputHref - href for view output tab
 * @param viewSqlHref - href for view sql tab
 * @param onEditFinished - handle edit finished
 */

export interface IViewEditorNavBarProps {
  i18nFinishButton: string;
  i18nViewOutputTab: string;
  i18nViewSqlTab: string;
  viewOutputHref: H.LocationDescriptor;
  viewSqlHref: H.LocationDescriptor;
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

  return (
    <Container
      style={{
        background: '#fff',
      }}
    >
      <Grid>
        <GridItem span={10}>
          <TabBar>
            <TabBarItem
              label={props.i18nViewOutputTab}
              to={props.viewOutputHref}
            />
            <TabBarItem
              label={props.i18nViewSqlTab}
              to={props.viewSqlHref}
            />
          </TabBar>
        </GridItem>
        <GridItem span={2}>
          <Button
            data-testid={'view-editor-navbar-done-button'}
            isDisabled={false}
            onClick={props.onEditFinished}
            variant={ButtonVariant.primary}
          >
            {props.i18nFinishButton}
          </Button>
        </GridItem>
      </Grid>
    </Container>
  );
}
