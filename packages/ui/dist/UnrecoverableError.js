import { EmptyState } from 'patternfly-react';
import * as React from 'react';
export const UnrecoverableError = () => (React.createElement(EmptyState, null,
    React.createElement(EmptyState.Icon, null),
    React.createElement(EmptyState.Title, null, "Something is wrong"),
    React.createElement(EmptyState.Info, null, "An error occurred while talking with the server."),
    React.createElement(EmptyState.Help, null, "Please check your internet connection."),
    React.createElement(EmptyState.Action, null,
        React.createElement("a", { href: '.', className: 'btn btn-lg btn-primary' }, "Refresh"))));
//# sourceMappingURL=UnrecoverableError.js.map