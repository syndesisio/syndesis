import * as React from 'react';
import { PageSection } from '../Layout';

export class IframeWrapper extends React.Component {
  public render() {
    return (
      <PageSection style={{ display: 'flex' }}>
        <div
          style={{
            background: '#fff',
            borderTop: '2px solid transparent',
            boxShadow: '0 1px 1px rgba(3, 3, 3, 0.175)',
            flex: '1',
          }}
        >
          {this.props.children}
        </div>
      </PageSection>
    );
  }
}
