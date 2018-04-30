Summary
Service to load corresponding log information regarding a microservice instance.

Deployment

Interface
Service can be reached by /cloudwatch-logs-adapter.
Available methods are:
 - Currently in work:
 - GET /groups/ Returns set of accepatable groupIDs
 - GET /groups/:id Returns metainformation of matching group
 - GET /groups/:id/streams/ Returns set of acceptable streamIDs
 - GET /groups/:id/streams/:id Returns metainformation of matching stream
 - GET /groups/:id/streams/:id/logs Returns logs of last 100 lines (100 is default to not overload connection. Override default by setting last Parameter by yourself as shown below)
 - GET /groups/:id/streams/:id/logs?last=:number Optional number of last logs to be served 
 - GET /streams/:id Returns List of metainformation of matching streams in groups
 - GET /streams/:id/logs Returns List of logs of last 100 lines of each stream identified by id
 - GET /streams/:id/logs?last=:number Optional number of last logs to be served for each occurence
 - GET /search/groups/:partialId
 - GET /search/streams/:partialId
 - Currently supported:
 - GET /<Microservice-Name> Returns last 20 lines of latest LogStream / LogFile being identified by the given MicroService-Name (String)

Note:
If Microservice-Name can be found in mutliple LogGroups only the first occurence will be found and returned.
If Microservice-Name can be found multiple times in LogStreams the most recent LogStream will be opened. Parralelly running instances and there LogStreams will be omitted.

