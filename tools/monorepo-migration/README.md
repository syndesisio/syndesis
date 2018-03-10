# Migration script

These scripts helped in migration from multiple distributed repos to a single monorepo.

I.e. these are:

* `merge_modules.sh` : Merge all modules via git operations into one directory, preserving the history
* `patch_pr.sh` : Move a PR from an old repo to the monorepo by creating and applying a patchfile.
* `move_issues.pl` : Move issues from single repos to monorepo in bulk
* `move_board.pl` : Use ZenHub API to mobe Epics to the monorepo and reattach issues to Epics

Look into the scripts at top-level, there is some short explanations.

## move_issues.pl

This script uses state file for remembering the issues already moved so that they can be restarted.
The directories `atlasmap` and `syndesis` hold the state files for those projects, as well as the specific configuration

Run it with

```
perl move_issues.pl --config syndesis/config.yml --state syndesis/issues_state.bin
perl move_issues.pl --config atlasmap/config.yml --state atlasmap/issues_state.bin
```

For looking into the statefile use this perl oneliner:

```
perl -MStorable -MData::Dumper -e 'print Dumper(retrieve("syndesis/issues_state.bin"))'
```


You might need to install some Perl modules to work with these scripts.
This is best done with `cpanm`.
