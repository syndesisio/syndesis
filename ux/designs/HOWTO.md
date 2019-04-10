## Howto

This directory is supposed to hold the UX designs for Syndesis features to implement. The format can be freely chosen, but it is suggested that for each design a dedicated directory is added, inluding a top-level markdown document which references singles screen within is.
See [global-settings-page](global-settings-page/global_settings_page_overview.md) for an example.

### Workflow

* Fork the GitHub repository with the GitHub UI "Fork" button in https://github.com/syndesisio/syndesis-ux . This creates a forked repository under your user name (e.g. https://github.com/rhuss/syndesis-ux)
* Clone your fork locally to your disk

```
git clone https://github.com/rhuss/syndesis-ux
```

* Create a branch for working on a specific design (you can freely choose the name)

```
git co -b pr/global-settings-page
```

* Create a new directory for your desing in `designs` directory (e.g. `global-settings-page`)

* Work on this branch and add your WIP designs to the `designs/global-settings-page` directoy
  - Add links to PNG design pages from within the README.md
  - Add PDFs directly in this directory, with the issue number as filename prefix (if any)

* If you are ready with review commit your work and push it to GitHub

```
git commit -m "initial version of the global settings page" -a
# For the first time push you need to create a remote branch with '-u'. This
# can be omitted for subsequent pushes
git push -u origin pr/global-settings-page
```

* Create a pull request on yours fork pull request page (e.g. `https://github.com/rhuss/syndesis-ux/pulls`) with button "New pull request". Choose your branch as the source branch and the "master" branch of `syndesisio/syndesis-project` as the target branch.

* If a task issue has been created to which this PR is attached, please attach the PR to this issue. Also use a "Fixes #..." within a _commit comment_ to automatically close the task issue as soon as the PR is merged.

* If there is no dedicated GitHub issue for the PR, connect it to the top-level Epic / User story (but don't add a 'Fixes' comment as above).

* Discussion and Reviews go now on on this pull request.

* When the review is done and the design is accepted the PR gets merged and is then available in the main repository.
