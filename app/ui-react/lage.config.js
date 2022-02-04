module.exports = {
  pipeline: {
    build: ["^build"],
    test: ["build"],
  },
  npmClient: "yarn",
};
