import { Card, EmptyState } from 'patternfly-react';
import * as React from 'react';
import ContentLoader from 'react-content-loader';
export const ConnectionSkeleton = (props) => (React.createElement(Card, { matchHeight: true },
    React.createElement(Card.Body, null,
        React.createElement(EmptyState, null,
            React.createElement(ContentLoader, Object.assign({ height: 300, width: 200, speed: 2, primaryColor: "#f3f3f3", secondaryColor: "#ecebeb" }, props),
                React.createElement("circle", { cx: "100", cy: "50", r: "40" }),
                React.createElement("rect", { x: "5", y: "125", rx: "5", ry: "5", width: "190", height: "30" }),
                React.createElement("rect", { x: "25", y: "180", rx: "5", ry: "5", width: "150", height: "15" }),
                React.createElement("rect", { x: "40", y: "205", rx: "5", ry: "5", width: "120", height: "15" }))))));
//# sourceMappingURL=ConnectionSkeleton.js.map