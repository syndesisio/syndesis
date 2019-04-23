import { WithVirtualization } from '@syndesis/api';
import { Container, Loader } from '@syndesis/ui';
import { WithLoader } from '@syndesis/utils';
import * as React from 'react';
import { ApiError } from '../../../shared';

export interface IWithVirtualizationDetailHeaderProps {
  virtualizationId: string;
}

export class HeaderView extends React.Component<
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
            errorChildren={<ApiError />}
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
