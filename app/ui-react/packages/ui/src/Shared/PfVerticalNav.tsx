// import * as React from 'react';
// import {
//   Nav,
//   NavList,
//   NavVariants
// } from '@patternfly/react-core';
// // import { PfVerticalNavItem } from './PfVerticalNavItem';

// export interface IPfVerticalNav {
//   items?: any[];
// }

// export interface IPfVerticalNavState {
//   activeItem: any;
// }

// class PfVerticalNav extends React.Component<IPfVerticalNav, IPfVerticalNavState> {
//   public onSelect: (result: any) => void;
//   constructor(props: IPfVerticalNav) {
//     super(props);
//     // this.state = {
//     //   activeItem: null
//     // };
//     this.onSelect = result => {
//       console.log(`need to set ${result.itemId} as active`);
//       console.log(`this this case ${this.state.activeItem}`);
//       // this.setState({
//       //   activeItem: result.itemId
//       // });
//     };
//   }

//   public render() {
//     return (
//       <Nav onSelect={this.onSelect}>
//         <NavList variant={NavVariants.simple}>
//           {this.props.children}
//           {/* {this.props.items.map(({exact, icon, label, to, key}) => {
//             return (
//               <PfVerticalNavItem
//                 exact={exact}
//                 icon={icon}
//                 label={label}
//                 to={to}
//                 key={key}

//               />
//             );
//           })} */}
//         </NavList>
//       </Nav>
//     );
//   }
// }

// export { PfVerticalNav };
