import * as React from 'react';
import equal from 'react-fast-compare';
export function callFetch({ url, method, headers = {}, body, contentType = 'application/json; charset=utf-8' }) {
    return fetch(url, {
        body: body ? JSON.stringify(body) : undefined,
        cache: 'no-cache',
        credentials: 'include',
        headers: {
            'Content-Type': contentType,
            ...headers
        },
        method,
        mode: 'cors',
        redirect: 'follow',
        referrer: 'no-referrer'
    });
}
export class Rest extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            data: this.props.defaultValue,
            error: false,
            hasData: false,
            loading: true,
            read: this.read,
            save: this.onSave
        };
        this.poller = this.poller.bind(this);
    }
    async componentDidMount() {
        if (this.props.autoload) {
            this.read();
            if (this.props.poll) {
                this.startPolling();
            }
        }
    }
    async componentDidUpdate(prevProps) {
        if (prevProps.url !== this.props.url) {
            this.read();
        }
        if (prevProps.poll !== this.props.poll) {
            if (this.props.poll) {
                this.startPolling();
            }
            else {
                this.stopPolling();
            }
        }
    }
    componentWillUnmount() {
        this.stopPolling();
    }
    shouldComponentUpdate(nextProps, nextState) {
        return !equal(this.props, nextProps) || !equal(this.state, nextState);
    }
    render() {
        return this.props.children(this.state);
    }
    async read() {
        try {
            this.setState({
                loading: true
            });
            const response = await callFetch({
                contentType: this.props.contentType,
                headers: this.props.headers,
                method: 'GET',
                url: `${this.props.baseUrl}${this.props.url}`
            });
            if (!response.ok) {
                throw new Error(response.statusText);
            }
            let data;
            if (!this.props.contentType ||
                this.props.contentType.indexOf('application/json') === 0) {
                data = await response.json();
            }
            else {
                data = await response.text();
            }
            this.setState({
                data,
                hasData: true,
                loading: false
            });
        }
        catch (e) {
            this.setState({
                data: this.props.defaultValue,
                error: true,
                errorMessage: e.message,
                hasData: false,
                loading: false
            });
        }
    }
    async onSave({ url, data }) {
        this.setState({
            loading: true
        });
        try {
            const response = await callFetch({
                body: data,
                contentType: this.props.contentType,
                headers: this.props.headers,
                method: 'PUT',
                url: `${this.props.baseUrl}${url || this.props.url}`
            });
            if (!response.ok) {
                throw new Error(response.statusText);
            }
            setTimeout(() => this.read(), 5000); // TODO: figure out why this is needed
        }
        catch (e) {
            this.setState({
                error: true,
                errorMessage: e.message,
                loading: false
            });
        }
    }
    startPolling() {
        this.stopPolling();
        this.pollingTimer = setInterval(this.poller, this.props.poll);
    }
    poller() {
        if (!this.state.loading) {
            this.read();
        }
    }
    stopPolling() {
        if (this.pollingTimer) {
            clearInterval(this.pollingTimer);
            this.pollingTimer = undefined;
        }
    }
}
Rest.defaultProps = {
    autoload: true
};
//# sourceMappingURL=Rest.js.map