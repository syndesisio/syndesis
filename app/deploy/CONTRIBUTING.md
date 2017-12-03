# CONTRIBUTING

## Setting up git hooks

After you have cloned the repository for the first time, it is advised to set up git pre-commit hooks. These checks will also run on our CI infrastructure, but to save time, some lightweight checks can run locally before you commit ready to submit a PR.

You only need to do this once!

This repo uses [pre-commit](http://pre-commit.com/) to manage git pre-commit hooks. Installation of pre-commit is simplest via [homebrew](https://brew.sh/) or [linuxbrew](http://linuxbrew.sh/):

```bash
$ brew install pre-commit
$ pre-commit install
```

Et voila... the configured pre-commit hooks will run every time you want to commit changes.
