import { WithVirtualization } from '@syndesis/api';
import { Container, Loader } from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';

export interface IWithVirtualizationDetailHeaderProps {
  virtualizationId: string;
}

export default class HeaderView extends React.Component<
  IWithVirtualizationDetailHeaderProps
> {
  public constructor(props: IWithVirtualizationDetailHeaderProps) {
    super(props);
  }

  public render() {
    return (
      <WithVirtualization virtualizationId={this.props.virtualizationId}>
        {({ data, hasData, error }) => (
          <WithLoader
            error={error}
            loading={!hasData}
            loaderChildren={<Loader />}
            errorChildren={<div>TODO</div>}
          >
            {() => (
              <Container>
                <h2>{data.keng__id}</h2>
                <h3>{data.tko__description}</h3>
              </Container>
            )}
          </WithLoader>
        )}
      </WithVirtualization>
    );
  }
}
