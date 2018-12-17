import { Spinner } from 'patternfly-react';
import * as React from 'react';

import './Loader.css';

export class Loader extends React.PureComponent {
  public render() {
    return (
      <div className={'Loader'}>
        <Spinner loading={true} size={'lg'} />
      </div>
    );
  }
}
