import {
  Category,
  CategoryLogger,
  CategoryServiceFactory,
  CategoryConfiguration,
  LogLevel
} from 'typescript-logging';

// TODO typescript-logging will support a console API to change this at runtime in the future, for now we'll use localStorage
let logLevel = LogLevel.Info;
try {
  const storedLogLevel =
    typeof localStorage !== 'undefined' && localStorage.getItem('logLevel');
  logLevel = LogLevel.fromString(storedLogLevel);
} catch (e) {
  // probably nothing has been set in the localStorage
}

// default configuration
CategoryServiceFactory.setDefaultConfiguration(
  new CategoryConfiguration(logLevel)
);

const rootCategory = new Category('root');
const categories = {};

// files/modules can import this function to create their own logging category
export function getCategory(name: string) {
  if (name in categories) {
    return categories[name];
  } else {
    categories[name] = new Category(name, rootCategory);
    return categories[name];
  }
}

// files should import this logger instance
export const log: CategoryLogger = CategoryServiceFactory.getLogger(
  rootCategory
);
