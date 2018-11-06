```
oc create -f <(echo '
kind: OAuthClient
apiVersion: oauth.openshift.io/v1
metadata:
 name: camel-k-ui
secret: "..."
redirectURIs:
 - "http://localhost:3000"
grantMethod: prompt
')
```