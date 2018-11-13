/// <reference types="react" />
interface IPfVerticalNavItem {
    className?: string;
    exact?: boolean;
    isActive?: (match: any, location: any) => boolean;
    icon: string;
    location?: any;
    strict?: boolean;
    to: string | any;
    label: any;
    children?: any;
}
declare function PfVerticalNavItem({ className, exact, isActive: isActiveProp, icon, location, strict, to, label, children, ...rest }: IPfVerticalNavItem): JSX.Element;
declare namespace PfVerticalNavItem {
    var displayName: any;
}
export { PfVerticalNavItem };
