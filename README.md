# unify_rabbitmq_crud

### kafka streams docs

https://kafka.apache.org/20/documentation/streams/quickstart
https://kafka.apache.org/20/documentation/streams/tutorial
https://kafka.apache.org/20/documentation/streams/developer-guide/interactive-queries.html#querying-local-state-stores-for-an-app-instance



https://kafka.apache.org/20/documentation/streams/developer-guide/testing.html
https://stackoverflow.com/questions/46220663/kstream-ktable-join-writing-to-the-ktable-how-to-sync-the-join-with-the-ktable
http://bigdatums.net/2017/05/21/send-key-value-messages-kafka-console-producer/
https://docs.confluent.io/3.1.1/streams/index.html
https://kafka.apache.org/10/documentation/streams/developer-guide/interactive-queries.html
https://docs.confluent.io/current/streams/concepts.html
https://docs.confluent.io/current/streams/architecture.html?_ga=2.265777096.696249922.1537392407-607151404.1536876543#state
https://docs.confluent.io/current/streams/kafka-streams-examples/docs/index.html?_ga=2.94790747.696249922.1537392407-607151404.1536876543


## TODOs for MVP

* Objects playground
    * [ ] Create test that will check for data consistency
    * [ ] Make 3 channels and pass them to single one with proper serdes
    
* Questions
    * [ ] Implement single channel aggregation with test
    * [ ] Make sure data is consistent
    * [ ] Join 3 CRUD topics
    * [ ] find kafka connect to RabbitMQ
    
    
* Prod consistency test:
    * [ ] prepare rabbitMQ to be run via docker
    * [ ] prepare kafka to be run in docker
    * [ ] prepare data in RabbitMQ, persist expected state
    * [ ] start kafka connectors and kafka stream
    * [ ] Verify results
    
* Prod performance test:
    * [ ] TODO fill this up
     
## Things to check at the end:
* [ ] make sure topics are partitioned by question id
