## Howto

This directory is supposed to hold the UX designs for Syndesis features to implement. The format can be freely chosen, but for ease of access it is suggested to add at least one final PDF artefact per design directly in this repository.

### Workflow

* Fork the GitHub repository with the GitHub UI "Fork" button in https://github.com/syndesisio/syndesis-project . This creates a forked repository under your user name (e.g. https://github.com/rhuss/syndesis-project)
* Clone your fork locally to your disk

```
git clone https://github.com/rhuss/syndesis-project`
```

* Create a branch for working on a specific design

```
git co -b pr/002-filter-step
```

* Work on this branch and add your WIP designs to the `design/` directoy
  - Add links to Invision designs to the README.md
  - Add PDFs directly in this directory, with the issue number as filename prefix

* If you are ready with review commit your work and push it to GitHub

```
git commit -m "initial version of filter step design" -a
# For the first time push you need to create a remote branch with '-u'. This 
# can be omitted for subsequent pushes
git push -u origin pr/002-filter-step
```

* Create a pull request on yours fork pull request page (e.g. `https://github.com/rhuss/syndesis-project/pulls`) with button "New pull request". Choose your branch as the source branch and the "master" branch of `syndesisio/syndesis-project` as the target branch.

* Discussion and Reviews go now on on this pull request. 

* When the review is done and the design is accepted the PR gets merged and is then available in the main repository.
