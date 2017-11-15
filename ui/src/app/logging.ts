import {
  Category,
  CategoryLogger,
  CategoryServiceFactory,
  CategoryDefaultConfiguration,
  LogLevel
} from 'typescript-logging';

// TODO typescript-logging will support a console API to change this at runtime in the future, for now we'll use localStorage
const storedLogLevel =
  typeof localStorage !== 'undefined' && localStorage.getItem('logLevel');
const logLevel = LogLevel[storedLogLevel] || LogLevel.Info;

// default configuration
CategoryServiceFactory.setDefaultConfiguration(
  new CategoryDefaultConfiguration(logLevel)
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
