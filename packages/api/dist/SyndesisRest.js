import * as React from 'react';
import { ApiContext } from "./ApiContext";
import { Rest } from './Rest';
import { Stream } from './Stream';
export class SyndesisRest extends React.Component {
    render() {
        const { url, stream, ...props } = this.props;
        const RestOrStream = stream ? Stream : Rest;
        return (React.createElement(ApiContext.Consumer, null, ({ apiUri, token }) => (React.createElement(RestOrStream, Object.assign({ baseUrl: apiUri, url: url }, props, { headers: {
                'SYNDESIS-XSRF-TOKEN': 'awesome',
                'X-Forwarded-Access-Token': `${token}`,
                'X-Forwarded-User': 'admin'
            } })))));
    }
}
//# sourceMappingURL=SyndesisRest.js.map