console.log("Starting migration to schema 30");

// Example upgrade invocation:

// var path = "/connectors";
// console.log("Fetching path: " + path);

// // Invoke helper jsondb class
// var obj = jsondb.get(path);

// Object.keys(obj).forEach(function(key,index) {
//     console.log("Properties for connector " + key);
//     var props = obj[key];
//     Object.keys(props["properties"]).forEach(function(key,index) {
//         console.log(key);
//     });
//     console.log("Adding new property: NEW_PROPERTY");
//     props["properties"]["NEW_PROPERTY"] = "new_value";
// });

// // Persist modification
// jsondb.update(path, obj);


console.log("Migration to schema 30 completed");