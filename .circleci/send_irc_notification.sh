#!/bin/bash

exec 5<>/dev/tcp/chat.freenode.net/6667
echo "USER syndesis-ci 8 * : Syndesis CircleCI" >&5
echo "NICK syndesis-ci" >&5
echo "PRIVMSG NickServ :IDENTIFY $IRC_NICKSERV_PASSWORD" >&5
sleep 5
echo "JOIN #syndesis" >&5

if [ -n "$CIRCLE_PR_NUMBER" ]; then
    echo "PRIVMSG #syndesis :😱 **PR $CIRCLE_PR_NUMBER** ($CIRCLE_PR_USERNAME) - Job $CIRCLE_JOB failed" >&5
    echo "PRIVMSG #syndesis :😱 https://github.com/syndesisio/syndesis/pull/$CIRCLE_PR_NUMBER" >&5
else
    echo "PRIVMSG #syndesis :😱 **Master** : Job $CIRCLE_JOB failed (last commit: $CIRCLE_USERNAME)" >&5
fi

echo "PRIVMSG #syndesis :😱 $CIRCLE_BUILD_URL" >&5
echo "QUIT" >&5
cat <&5
