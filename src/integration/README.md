# Run integration tests:

* `make setup-clusters`
* `make topics_setup` 
* `make start_rabbit` 
* run `FullConsistencyTest`
* run connector:
    * `.../confluent-5.0.0/bin/connect-standalone connectors/worker-working.properties connectors/create-questions.properties connectors/update-questions.properties connectors/delete-questions.properties`
* wait for test to finish.
