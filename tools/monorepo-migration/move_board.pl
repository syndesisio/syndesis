#!/usr/bin/perl

use Storable;
use HTTP::Tiny;
use Data::Dumper;
use strict;
use JSON::Tiny qw(decode_json encode_json);

my $REPOS = {
             "rest" => "75404146",
             "project" => "92834472",
             "ui" => "95247826",
             "verifier" => "85746431",
             "uxd" => "96111131",
             "deploy" => "80314524",
             "connectors" => "81928401",
             "runtime" => "96436677"
            };

my $REPO_REVERSE_MAP = {};

for my $k (keys %$REPOS) {
    $REPO_REVERSE_MAP->{$REPOS->{$k}} = $k;
}

my $token = "insert your token from https://dashboard.zenhub.io/#/settings here";
my $headers = {
              "X-Authentication-Token" => $token
              };
my $base = "https://api.zenhub.io/p1";

my $http = HTTP::Tiny->new();


my $mapping = &load_state();

my $target_repo = 105563335;
my $response = $http->get($base . "/repositories/$target_repo/board", { headers => $headers });
my $target_board = decode_json $response->{content};
my $target_pipelines = {};
for my $pipeline (@{$target_board->{pipelines}}) {
    $target_pipelines->{$pipeline->{name}} = $pipeline->{id};
}

my $repo = "project";
&move_issue_to_pipelines($repo, $mapping);


sub move_issue_to_pipelines {
    my $repo = shift;
    my $mapping = shift;
    my $response = $http->get($base . "/repositories/" . $REPOS->{$repo} . "/board", { headers => $headers });
    my $board = decode_json $response->{content};
    for my $pipeline (@{$board->{pipelines}}) {
        my $name = $pipeline->{name};
        my $target_pipeline = $target_pipelines->{$name};
        for my $issue (@{$pipeline->{issues}}) {
            my $old_id = $issue->{issue_number};
            my $is_epic = $issue->{is_epic};
            my $new_id = $mapping->{$repo}->{$old_id}->{new_id};
            print "$name: $old_id -> $new_id ($is_epic)\n";
#            next if $name eq "New Issues";
            if ($is_epic) {
                my $response = $http->get($base . "/repositories/" . $REPOS->{$repo} . "/epics/" . $old_id, {headers => $headers});
#                print Dumper($response);
                my $epic = decode_json $response->{content};
                my @epic_issues = ();
                for my $issue (@{$epic->{issues}}) {
                    my $old_issue_id = $issue->{issue_number};
                    my $issue_repo = $REPO_REVERSE_MAP->{$issue->{repo_id}};
                    print "Y:$issue_repo\n";
                    my $new_issue_id = $mapping->{$issue_repo}->{$old_issue_id}->{new_id};
                    print "X:$new_issue_id\n";
                    if ($new_issue_id) {
                        push @epic_issues,{ repo_id => $target_repo, issue_number => $new_issue_id };
                    } else {
                        push @epic_issues,{ repo_id => $issue->{repo_id}, issue_number => $old_issue_id };
                    }
                }
                my $body = "" . &encode_json({ issues => \@epic_issues });
                print "Body: $body\n";
                my $response = $http->post(
                                           $base . "/repositories/$target_repo/issues/$new_id/convert_to_epic",
                                             {
                                              headers => { %$headers, 'content-type' => 'application/json'},
                                              content => $body
                                             });
                die Dumper($response) until $response->{success};
                my $response = $http->post($base . "/repositories/$target_repo/issues/$new_id/moves",
                                             {
                                              headers => { %$headers, 'content-type' => 'application/json'},
                                              content => &encode_json({ pipeline_id => $target_pipeline,
                                                                        position => "bottom" })
                                             });
                die Dumper($response) until $response->{success};
#                exit 0;
            }
        }
    }

}


sub load_state {
    my $file = "issues_state.bin";
    if (-f $file) {
        return retrieve($file);
    } else {
        return {};
    }
}
